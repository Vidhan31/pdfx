package com.pdfx.pdf.metadata;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.metadata.Metadata;
import com.pdfx.metadata.MetadataEditor;
import com.pdfx.metadata.MetadataRetriever;
import com.pdfx.pdfutil.PdfPreviews;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import com.pdfx.util.SelectionView;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;

import static javafx.scene.control.Alert.AlertType.ERROR;

public class MetadataController {

    @FXML
    private TextField authorField;

    @FXML
    private DatePicker createDatePicker;

    @FXML
    private TextField creatorField;

    @FXML
    private Button editButton;

    @FXML
    private TextArea keywordsTextArea;

    @FXML
    private DatePicker modifyDatePicker;

    @FXML
    private ImageView pdfImageView;

    @FXML
    private TextField producerField;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField subjectField;

    @FXML
    private TextField titleField;

    @FXML
    private Label fileNameLabel;

    private void displayPreview() {

        var files = FilesManagement.getFileListByType(FileType.PDF).orElseThrow();
        var executorService = SingletonExecutorService.getInstance();

        Task<BufferedImage> previewTask = new Task<>() {

            @Override
            protected BufferedImage call() {

                try {
                    return PdfPreviews.generateSinglePDFPreview(files.get(0), 205, 241);
                } catch (EncryptedFileException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Cannot read file", "PDF file is encrypted", "Decrypt them and then select", ERROR);
                } catch (IOException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Files not Found", "Only valid PDF Files are allowed", "Select pdf files and try again", ERROR);
                }
                return null;
            }
        };

        executorService.submit(previewTask);
        previewTask.setOnSucceeded(workerStateEvent -> {

            try {
                pdfImageView.setImage(SelectionView.convertToFxImage(previewTask.get()).getImage());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            fileNameLabel.setText(files.get(0).getName());
        });
    }

    private void setTooltips() {

        authorField.setTooltip(new Tooltip(authorField.getText()));
        producerField.setTooltip(new Tooltip(producerField.getText()));
        creatorField.setTooltip(new Tooltip(creatorField.getText()));
        producerField.setTooltip(new Tooltip(producerField.getText()));
        subjectField.setTooltip(new Tooltip(subjectField.getText()));
        titleField.setTooltip(new Tooltip(titleField.getText()));
        keywordsTextArea.setTooltip(new Tooltip(keywordsTextArea.getText()));
    }

    private void fillValues() throws IOException {

        var file = FilesManagement.getFileListByType(FileType.PDF).orElseThrow().get(0);
        var metadata = new MetadataRetriever(file).retrieve();
        authorField.setText(metadata.author());
        creatorField.setText(metadata.creator());
        producerField.setText(metadata.producer());
        subjectField.setText(metadata.subject());
        titleField.setText(metadata.title());
        keywordsTextArea.setText(metadata.keywords());
        try {
            createDatePicker.setValue(getLocalDate(metadata.creation()));
        } catch (NullPointerException e) {
            createDatePicker.setValue(null);
        }
        try {
            modifyDatePicker.setValue(getLocalDate(metadata.modification()));
        } catch (NullPointerException e) {
            modifyDatePicker.setValue(null);
        }
    }

    private void setEditMode(boolean flag) {

        authorField.setDisable(flag);
        createDatePicker.setDisable(flag);
        creatorField.setDisable(flag);
        keywordsTextArea.setDisable(flag);
        modifyDatePicker.setDisable(flag);
        producerField.setDisable(flag);
        subjectField.setDisable(flag);
        titleField.setDisable(flag);

        editButton.setDisable(!flag);
        saveButton.setVisible(!flag);
        cancelButton.setVisible(!flag);
    }

    private LocalDate getLocalDate(Calendar calendar) {

        // Extract year, month, and day from the Calendar
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a LocalDate object using the extracted values
        return LocalDate.of(year, month + 1, day);
    }

    private Calendar getCalendar(DatePicker datePicker) {

        // Retrieve the selected LocalDate value from the DatePicker
        LocalDate selectedDate = datePicker.getValue();

        // Convert the LocalDate to a ZonedDateTime by specifying time and zone
        ZonedDateTime zonedDateTime = selectedDate.atStartOfDay(ZoneId.systemDefault());

        // Convert the ZonedDateTime to a Calendar instance
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(zonedDateTime.toInstant().toEpochMilli());
        return calendar;
    }

    @FXML
    private void initialize() throws IOException {

        setEditMode(true);
        fillValues();
        setTooltips();
        displayPreview();
    }

    @FXML
    private void handleEditButton() {

        setEditMode(false);
    }

    @FXML
    private void handleCancelButton() {

        setEditMode(true);
    }

    @FXML
    private void handleCloseButton() {

        titleField.getScene().getWindow().hide();
    }

    @FXML
    private void handleSaveButton() throws IOException {

        var file = FilesManagement.getFileListByType(FileType.PDF).orElseThrow().get(0);
        var title = titleField.getText();
        var author = authorField.getText();
        var subject = subjectField.getText();
        var creator = creatorField.getText();
        var producer = producerField.getText();
        var keywords = keywordsTextArea.getText();
        var creation = getCalendar(createDatePicker);
        var modification = getCalendar(modifyDatePicker);
        var metadata = new Metadata(file, title, author, subject, creator, producer, keywords, creation, modification);
        new MetadataEditor(metadata).edit();
        setTooltips();
    }
}
