package com.pdfx.pdf.merge;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.exceptions.FileNotSelectedException;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.home.progress.CompletedProgressView;
import com.pdfx.home.progress.IndeterminateProgressView;
import com.pdfx.logging.StackTraceUtil;
import com.pdfx.merge.MergePdfs;
import com.pdfx.merge.MergerFiles;
import com.pdfx.pdfutil.PdfPreviews;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import com.pdfx.util.FilePicker;
import com.pdfx.util.SelectionView;
import com.pdfx.view.ViewManager;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.stage.Modality;

import static com.pdfx.view.View.HOME;
import static com.pdfx.view.View.SPLIT;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class MergePdfController {

    @FXML
    private FlowPane flowPane;

    @FXML
    private CheckBox mergeAllCheckbox;

    @FXML
    private ProgressIndicator thumbnailProgressIndicator;

    private SelectionView selectionView;

    @FXML
    private void initialize() {

        showPreviews();
    }

    /**
     * Fills scene with previews for user to select pdf pages of multiple pdf documents.
     */
    private void showPreviews() {

        ExecutorService executorService = SingletonExecutorService.getInstance();
        thumbnailProgressIndicator.setVisible(true);

        Task<List<BufferedImage>> task = new Task<>() {

            @Override
            protected List<BufferedImage> call() {

                try {
                    var files = FilesManagement.getFileListByType(FileType.PDF).orElseThrow();
                    var pdfPreviews = new PdfPreviews(files);
                    return pdfPreviews.generateMultiplePDFPreviews(198, 241);

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("File Processing error", "Unable to load the file", "Please check if it is a valid pdf file", ERROR);
                } catch (EncryptedFileException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Cannot read file", "PDF file is encrypted", "Decrypt them and then select", ERROR);
                } catch (NullPointerException | IOException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again", ERROR);
                }
                return null;
            }
        };

        executorService.submit(task);
        task.setOnSucceeded(workerStateEvent -> {

            thumbnailProgressIndicator.setVisible(false);
            try {
                selectionView = new SelectionView(flowPane, task.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleReselectFilesButtonClick() {

        try {
            FilePicker.chooseFiles(flowPane);
        } catch (InterruptedException | ExecutionException e) {

            CustomAlerts.alert("Invalid File Format", "Please select valid pdf file", "", ERROR);
            e.printStackTrace();
        }

        if (FilesManagement.containsFileType(FileType.PDF)) {

            if (FilesManagement.getTotalFilesNumber(FileType.PDF) > 1) {

                flowPane.getChildren().clear();
                showPreviews();
            } else
                CustomAlerts.alert("Insufficient Files", "Merging requires 2 or more pdf files", "Please reselect and try again", WARNING);

        } else
            CustomAlerts.alert("Files processing error", "PDF Files not found", "Select pdf files and try again", ERROR);
    }

    @FXML
    private void handleMergeButtonClick() throws FileNotSelectedException {

        var order = selectionView.getSelections().stream().map(s -> Integer.parseInt(s) + 1).toList();

        if (!mergeAllCheckbox.isSelected()) {

            if (order.isEmpty() || order.size() < 2) {

                CustomAlerts.alert("Selection Error", "Merging requires atleast 2 files to be selected", "Try selecting two or more", WARNING);
                return;
            }
        }

        String filename = takeUserInput();

        var files = FilesManagement.getFileListByType(FileType.PDF).orElseThrow(FileNotSelectedException::new);
        int total = files.size();

        IndeterminateProgressView indeterminateProgressView = new IndeterminateProgressView();
        indeterminateProgressView.TaskDescriptionProperty().set("Merging " + total + " pdf documents");
        var progressBarProperty = indeterminateProgressView.ProgressProperty();

        var executorService = SingletonExecutorService.getInstance();

        Task<Boolean> mergeTask = new Task<>() {

            @Override
            protected Boolean call() {

                try {

                    return new MergePdfs(new MergerFiles(files, order, filename)).merge(mergeAllCheckbox.isSelected());

                } catch (IOException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again", ERROR);
                } catch (NullPointerException e) {
                    CustomAlerts.alert("Selection Error", "Select pdf page before merging", "Try merging again", WARNING);
                }
                return null;
            }
        };

        executorService.submit(mergeTask);
        progressBarProperty.bind(mergeTask.progressProperty());

        mergeTask.setOnSucceeded(workerStateEvent -> {

            try {

                indeterminateProgressView.finish();
                if (mergeTask.get())
                    new CompletedProgressView(String.valueOf(total).concat(" pdf documents have been merged"),
                            files.get(0).getParent());

            } catch (NullPointerException e) {

                e.printStackTrace();
                CustomAlerts.alert("Access Denied", "Invalid files/directory path", "Files may have been moved or deleted", ERROR);
            } catch (ExecutionException | InterruptedException e) {

                e.printStackTrace();
                CustomAlerts.alert("Merging Error", "Trouble extracting from files. try again", StackTraceUtil.getStackTrace(e));
            }

            try {
                handleBackButtonClick();
            } catch (IOException e) {
                e.printStackTrace();
                CustomAlerts.alert("Internal error", "Unable to go back. Restart application", StackTraceUtil.getStackTrace(e));
            } finally {
                FilesManagement.removeFileListByType(FileType.PDF);
            }
        });
    }

    private String takeUserInput() {

        var textInputDialog = new TextInputDialog();
        textInputDialog.getDialogPane().getStylesheets().add(getClass().getResource("/fxml/css/nord-dark.css").toExternalForm());
        textInputDialog.setHeaderText("Enter name of the new file or leave it blank for a default name");
        textInputDialog.initModality(Modality.WINDOW_MODAL);
        textInputDialog.initOwner(flowPane.getScene().getWindow());
        return getFileName(textInputDialog.showAndWait().orElse(""));
    }

    @FXML
    private void handleBackButtonClick() throws IOException {

        var viewManager = ViewManager.getInstance(null);
        ViewManager.removeView(SPLIT);
        viewManager.showView(HOME, "HomePage", true, false);
        flowPane.getChildren().clear();
    }

    private String getFileName(String filename) {

        if (filename.isBlank()) {

            var dateTimeFormatter = DateTimeFormatter.ofPattern("dd_MM_yyyy_HH_mm_ss");
            LocalDateTime now = LocalDateTime.now();
            filename = "Merged file ".concat(dateTimeFormatter.format(now).concat(".pdf"));
        } else {

            if (!filename.contains(".pdf"))
                filename = filename.concat(".pdf");
        }
        return filename;
    }
}
