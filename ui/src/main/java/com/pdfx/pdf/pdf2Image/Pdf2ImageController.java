package com.pdfx.pdf.pdf2Image;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.home.progress.CompletedProgressView;
import com.pdfx.home.progress.DeterminateProgressView;
import com.pdfx.logging.StackTraceUtil;
import com.pdfx.pdf2image.Pdf2Image;
import com.pdfx.pdf2image.Pdf2ImageFiles;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;

public class Pdf2ImageController {

    @FXML
    private ComboBox<String> outputFormatComboBox;

    @FXML
    private RadioButton defaultRadio;

    @FXML
    private RadioButton highRadio;

    @FXML
    private RadioButton lowRadio;

    /**
     * Populates Combobox and pre-selects first option
     */
    @FXML
    private void initialize() {

        outputFormatComboBox.setItems(FXCollections.observableList(Arrays.asList("JPG", "JPEG", "PNG")));
        outputFormatComboBox.getSelectionModel().selectFirst();
    }

    /**
     * Runs pdf to image conversion task
     */
    @FXML
    private void handleConvertButtonClick() {

        var fileList = getFilesToBeConverted();
        int total = fileList.size();

        DeterminateProgressView determinateProgressView = new DeterminateProgressView();
        var progressBarProperty = determinateProgressView.ProgressProperty();
        determinateProgressView.TaskDescriptionProperty().set("out of " + total + " pdf files converted to " + getOutputFormat());

        var executorService = SingletonExecutorService.getInstance();

        Task<Integer> convert2ImageTask = new Task<>() {

            @Override
            protected Integer call() {

                IntegerProperty integerProperty = new SimpleIntegerProperty(0);
                integerProperty.addListener((observableValue, number, t1) -> {

                    updateProgress(t1.longValue(), total);
                    updateMessage(String.valueOf(t1.intValue()));
                });

                try {
                    new Pdf2Image(new Pdf2ImageFiles(fileList, getDpi(), getOutputFormat(), integerProperty)).convert();

                } catch (IOException e) {
                    CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again"
                            , Alert.AlertType.ERROR);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Conversion Error", "There was error processing some file/pages",
                            StackTraceUtil.getStackTrace(e));
                } catch (EncryptedFileException e) {
                    CustomAlerts.alert("Cannot read file", "PDF file is encrypted", "Decrypt them and then convert",
                            Alert.AlertType.ERROR);
                }
                return integerProperty.get();
            }
        };

        progressBarProperty.bind(convert2ImageTask.progressProperty());
        determinateProgressView.DynamicTextProperty().bind(convert2ImageTask.messageProperty());

        executorService.execute(convert2ImageTask);

        convert2ImageTask.setOnSucceeded(workerStateEvent -> {

            try {

                determinateProgressView.finish();
                if (convert2ImageTask.get() > 0)
                    new CompletedProgressView(convert2ImageTask.get() + " PDF files have been converted to " + getOutputFormat(), fileList.get(0).getParent());

            } catch (NullPointerException e) {

                e.printStackTrace();
                CustomAlerts.alert("Access Denied", "Invalid file/directory path", "Files may have been moved or deleted",
                        Alert.AlertType.ERROR);
            } catch (ExecutionException | InterruptedException e) {

                e.printStackTrace();
                CustomAlerts.alert("Extraction Error", "Trouble extracting from file. try again",
                        StackTraceUtil.getStackTrace(e));
            } finally {
                FilesManagement.removeFileListByType(FileType.PDF);
                outputFormatComboBox.getScene().getWindow().hide();
            }
        });
    }

    private ObservableList<File> getFilesToBeConverted() {

        if (!FilesManagement.containsFileType(FileType.PDF))
            CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again",
                    Alert.AlertType.ERROR);

        return FXCollections.observableList(FilesManagement.getFileListByType(FileType.PDF).orElseThrow());
    }

    private String getOutputFormat() {

        return outputFormatComboBox.getSelectionModel().getSelectedItem().toLowerCase();
    }

    private int getDpi() {

        if (defaultRadio.isSelected())
            return 150;

        else if (highRadio.isSelected())
            return 300;

        else if (lowRadio.isSelected())
            return 72;

        else
            return 100;
    }
}
