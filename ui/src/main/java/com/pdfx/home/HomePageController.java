package com.pdfx.home;

import com.pdfx.exceptions.AccessPermissionsException;
import com.pdfx.extract.images.ImageExtraction;
import com.pdfx.extract.images.ImageExtractionFiles;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.fileshandler.FilesValidation;
import com.pdfx.home.progress.CompletedProgressView;
import com.pdfx.home.progress.DeterminateProgressView;
import com.pdfx.home.progress.ProgressViewInitializer;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import com.pdfx.util.FilePicker;
import com.pdfx.view.View;
import com.pdfx.view.ViewManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;

import static com.pdfx.util.CustomAlerts.alert;
import static com.pdfx.view.View.EXTRACT;
import static com.pdfx.view.View.MERGE;
import static com.pdfx.view.View.SPLIT;
import static javafx.scene.control.Alert.AlertType.ERROR;

public class HomePageController {

    @FXML
    private BorderPane borderPane;

    @FXML
    private ScrollPane scrollPane;

    /**
     * Initializes ProgressLayout constructor to load Vbox which can later be used to add new Progress bar interfaces.
     */
    @FXML
    public void initialize() {

        new ProgressViewInitializer(scrollPane);
    }

    @FXML
    private void handleSelectFilesButtonClick() {

        try {
            FilePicker.chooseFiles(borderPane);
        } catch (InterruptedException | ExecutionException e) {

            CustomAlerts.alert("File loading error", "Select valid pdf file", "Try selecting again", ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDragDrop(DragEvent event) throws ExecutionException, InterruptedException {

        if (event.getDragboard().hasFiles()) {

            var selectedFiles = event.getDragboard().getFiles();
            var filesValidation = new FilesValidation(selectedFiles, "pdf");

            if (filesValidation.acceptFiles())
                event.acceptTransferModes(TransferMode.COPY);

            event.consume();
        }
        event.consume();
    }

    @FXML
    private void handleHideProgress() {

        borderPane.getRight().prefWidth(borderPane.getWidth() >= 315 ? 0 : 315);
    }

    private void showCustomDialog(String fxml) throws IOException {

        try {

            if (FilesManagement.containsFileType(FileType.PDF)) {

                var viewManager = ViewManager.getInstance(null);
                var customDialog = viewManager.loadDialog("/fxml/".concat(fxml));
                customDialog.show();
                customDialog.getDialogPane().getScene().getWindow().setOnCloseRequest(windowEvent -> {

                    borderPane.setEffect(null);
                    customDialog.close();
                });
                borderPane.setEffect(new BoxBlur(3, 3, 3));

            } else
                CustomAlerts.alert("Files not found", "No Pdf Files were found", "Please retry selecting valid pdf files",
                        ERROR);

        } catch (NullPointerException e) {
            CustomAlerts.alert("Files not found", "No Pdf Files were found", "Please retry selecting valid pdf files", ERROR);
        }
    }

    private void showView(String fxml, View view, String title, boolean resizeable, boolean maximized) throws IOException {

        try {

            if (FilesManagement.containsFileType(FileType.PDF)) {

                var viewManager = ViewManager.getInstance(null);
                viewManager.loadNewView("/fxml/".concat(fxml), view);
                viewManager.showView(view, title, resizeable, maximized);
            } else
                CustomAlerts.alert("Files not found", "No Pdf Files were found", "Please retry selecting valid pdf files",
                        ERROR);

        } catch (NullPointerException e) {
            CustomAlerts.alert("Files not found", "No Pdf Files were found", "Please retry selecting valid pdf files", ERROR);
        }
    }

    @FXML
    private void handleSplitPdfCardClick() throws IOException {

        showView("Split.fxml", SPLIT, "Split Pdf Document", true, false);
    }

    @FXML
    private void handleExtractTextCardClick() throws IOException {

        showView("Extract2Text.fxml", EXTRACT, "Copy extracted content to clipboard or text file", true, false);
    }

    @FXML
    private void handleMergePdfCardClick() throws IOException {

        showView("Merge.fxml", MERGE, "Merge two or more Pdf Document", true, false);
    }

    @FXML
    private void handleMetadataCardClick() throws IOException {

        showCustomDialog("MetadataEditor.fxml");
    }

    @FXML
    private void handleCompressCardClick() throws IOException {

        showCustomDialog("Compress.fxml");
    }

    @FXML
    private void handlePdfToImageCardClick() throws IOException {

        showCustomDialog("Pdf2Image.fxml");
    }

    @FXML
    private void handleEncryptionCardClick() throws IOException {

        showCustomDialog("Encryption.fxml");
    }

    @FXML
    private void handleDecryptionCardClick() throws IOException {

        showCustomDialog("Decryption.fxml");
    }

    @FXML
    private void handleExtractImagesCardClick() {

        File destination;
        try {

            destination = FilePicker.chooseDirectory(borderPane);
        } catch (FileNotFoundException e) {

            CustomAlerts.alert("Processing error", "No directory selected", "Select directory and try again", ERROR);
            e.printStackTrace();
            return;
        }

        var fileList = FilesManagement.getFileListByType(FileType.PDF).orElseThrow();
        var determinateProgress = new DeterminateProgressView();
        var executorService = SingletonExecutorService.getInstance();

        Task<Void> task = new Task<>() {

            @Override
            protected Void call() {

                try {

                    updateMessage("Extracting Images...");
                    new ImageExtraction(new ImageExtractionFiles(fileList, destination, true, true)).extract();
                } catch (AccessPermissionsException e) {
                    CustomAlerts.alert("Access Denied", "Check if the pdf has owner password", "If yes, decrypt it using Decrypt Tool", ERROR);
                    e.printStackTrace();
                } catch (IOException | InterruptedException e) {
                    CustomAlerts.alert("Extraction Error", "Some Images may not have been extracted", "Try individually for those missed files", ERROR);
                }
                return null;
            }
        };

        executorService.submit(task);
        determinateProgress.ProgressProperty().bind(task.progressProperty());
        determinateProgress.DynamicTextProperty().bind(task.messageProperty());

        task.setOnSucceeded(event -> {

            determinateProgress.finish();
            new CompletedProgressView("Images have been extracted", fileList.get(0).getParent());
        });
    }
}
