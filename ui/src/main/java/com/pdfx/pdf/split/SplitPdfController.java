package com.pdfx.pdf.split;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.exceptions.FileNotSelectedException;
import com.pdfx.extract.pages.PageExtraction;
import com.pdfx.extract.pages.PageExtractionFiles;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.home.progress.CompletedProgressView;
import com.pdfx.home.progress.DeterminateProgressView;
import com.pdfx.logging.StackTraceUtil;
import com.pdfx.pdfutil.PdfPreviews;
import com.pdfx.split.SplitPdf;
import com.pdfx.split.SplitPdfFiles;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import com.pdfx.util.SelectionView;
import com.pdfx.view.ViewManager;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;

import static com.pdfx.view.View.HOME;
import static com.pdfx.view.View.SPLIT;
import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class SplitPdfController {

    @FXML
    private FlowPane flowPane;

    @FXML
    private ToggleButton splitToggleButton;

    @FXML
    private ProgressIndicator thumbnailProgressIndicator;

    @FXML
    private CheckBox extractAllCheckbox;

    private SelectionView selectionView;

    @FXML
    private void initialize() {

        showPreviews();
    }

    /**
     * Fills scene with previews for user to select pdf pages of a single document.
     */
    private void showPreviews() {

        ExecutorService executorService = SingletonExecutorService.getInstance();
        thumbnailProgressIndicator.setVisible(true);

        Task<List<BufferedImage>> task = new Task<>() {

            @Override
            protected List<BufferedImage> call() {

                try {
                    List<File> files = FilesManagement.getFileListByType(FileType.PDF).orElseThrow();
                    PdfPreviews pdfPreviews = new PdfPreviews(files);
                    return pdfPreviews.generateAllPagesPDFPreviews(200, 245);

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("File Processing error", "Unable to load the file", "Please check if it is a valid pdf " +
                            "file", ERROR);
                } catch (EncryptedFileException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Cannot read file", "PDF file is encrypted", "Decrypt them and then select", ERROR);
                } catch (IOException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again"
                            , ERROR);
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

    /**
     * Calls the method responsible for fulfilling user request.
     */
    @FXML
    private void handleSplitButtonClick() throws FileNotSelectedException {

        if (!extractAllCheckbox.isSelected())
            if (selectionView.getSelections() == null || selectionView.getSelections().isEmpty())
                CustomAlerts.alert("Selection Error", "Select pdf page before splitting", "Try again", WARNING);

        if (splitToggleButton.isSelected())
            handleSplit();
        else
            handleExtraction();
    }

    /**
     * Runs Pdf splitting task
     */
    private void handleSplit() throws FileNotSelectedException {

        File file = FilesManagement.getFileListByType(FileType.PDF).orElseThrow(FileNotSelectedException::new).get(0);
        DeterminateProgressView determinateProgressView = new DeterminateProgressView();
        var progressBarProperty = determinateProgressView.ProgressProperty();
        determinateProgressView.TaskDescriptionProperty().set("splited from " + file.getName());

        var executorService = SingletonExecutorService.getInstance();

        Task<Boolean> splitTask = new Task<>() {

            @Override
            protected Boolean call() {

                var stringProperty = new SimpleStringProperty();
                determinateProgressView.DynamicTextProperty().bind(stringProperty);
                var selections = selectionView.getSelections().stream().map(s -> Integer.parseInt(s) + 1).toList();

                try {
                    return new SplitPdf(new SplitPdfFiles(file, selections, stringProperty)).split();
                } catch (IOException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again"
                            , ERROR);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Splitting Error", "Check if its a valid Pdf file and try again",
                            StackTraceUtil.getStackTrace(e));
                } catch (EncryptedFileException e) {
                    CustomAlerts.alert("Cannot read file", "PDF file is encrypted", "Decrypt and then select", ERROR);
                }
                return null;
            }
        };

        executorService.submit(splitTask);
        progressBarProperty.bind(splitTask.progressProperty());

        splitTask.setOnSucceeded(workerStateEvent -> {

            try {

                determinateProgressView.finish();
                if (splitTask.get())
                    new CompletedProgressView(file.getName() + " has been successfully splitted", file.getParent());

            } catch (NullPointerException e) {

                e.printStackTrace();
                CustomAlerts.alert("Access Denied", "Invalid file/directory path", "Files may have been moved or deleted",
                        ERROR);
            } catch (ExecutionException | InterruptedException e) {

                e.printStackTrace();
                CustomAlerts.alert("Extraction Error", "Trouble extracting from file. try again",
                        StackTraceUtil.getStackTrace(e));
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

    /**
     * Runs Pdf Extraction task
     */
    private void handleExtraction() throws FileNotSelectedException {

        File file = FilesManagement.getFileListByType(FileType.PDF).orElseThrow(FileNotSelectedException::new).get(0);
        DeterminateProgressView determinateProgressView = new DeterminateProgressView();
        determinateProgressView.TaskDescriptionProperty().set(" is being extracted from " + file.getName());
        var progressBarProperty = determinateProgressView.ProgressProperty();

        var executorService = SingletonExecutorService.getInstance();

        Task<Boolean> extractTask = new Task<>() {

            @Override
            protected Boolean call() {

                var stringProperty = new SimpleStringProperty();
                determinateProgressView.DynamicTextProperty().bind(stringProperty);

                try {

                    var selections = selectionView.getSelections().stream().map(s -> Integer.parseInt(s) + 1).toList();
                    if (extractAllCheckbox.isSelected())
                        return new PageExtraction(new PageExtractionFiles(file, Optional.empty(), stringProperty)).extractAll();
                    else
                        return new PageExtraction(new PageExtractionFiles(file, Optional.of(selections), stringProperty)).extract();

                } catch (IOException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again"
                            , ERROR);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Extraction Error", "Check if its a valid Pdf file and try again",
                            StackTraceUtil.getStackTrace(e));
                } catch (EncryptedFileException e) {
                    CustomAlerts.alert("Cannot read file", "PDF file is encrypted", "Decrypt and then select", ERROR);
                } catch (FileNotSelectedException e) {
                    CustomAlerts.alert("Selection Error", "Select pdf page before splitting", "Try splitting again", WARNING);
                }
                return null;
            }
        };

        executorService.submit(extractTask);
        progressBarProperty.bind(extractTask.progressProperty());

        extractTask.setOnSucceeded(workerStateEvent -> {

            try {

                determinateProgressView.finish();
                if (extractTask.get())
                    new CompletedProgressView("Page(s) from " + file.getName() + " has been successfully extracted", file.getParent());

            } catch (NullPointerException e) {

                e.printStackTrace();
                CustomAlerts.alert("Access Denied", "Invalid file/directory path", "Files may have been moved or deleted",
                        ERROR);
            } catch (ExecutionException | InterruptedException e) {

                e.printStackTrace();
                CustomAlerts.alert("Extraction Error", "Trouble extracting from file. try again",
                        StackTraceUtil.getStackTrace(e));
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

    @FXML
    private void handleBackButtonClick() throws IOException {

        var viewManager = ViewManager.getInstance(null);
        ViewManager.removeView(SPLIT);
        viewManager.showView(HOME, "HomePage", true, false);
        flowPane.getChildren().clear();
    }

    @FXML
    private void handleToggleSelection() {

        extractAllCheckbox.setDisable(splitToggleButton.isSelected());
    }
}
