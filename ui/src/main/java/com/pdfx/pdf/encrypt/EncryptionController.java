package com.pdfx.pdf.encrypt;

import com.pdfx.encrypt.Encryption;
import com.pdfx.encrypt.EncryptionData;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.home.progress.CompletedProgressView;
import com.pdfx.home.progress.DeterminateProgressView;
import com.pdfx.logging.StackTraceUtil;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import com.pdfx.util.FilePicker;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;

import static javafx.scene.control.Alert.AlertType.ERROR;

public class EncryptionController {

    @FXML
    private RadioButton aesRadio;

    @FXML
    private CheckBox canManipulate;

    @FXML
    private CheckBox extractable;

    @FXML
    private RadioButton highRadio;

    @FXML
    private CheckBox isFillable;

    @FXML
    private RadioButton lowRadio;

    @FXML
    private CheckBox modifiable;

    @FXML
    private PasswordField ownerPasswordField;

    @FXML
    private CheckBox printable;

    @FXML
    private PasswordField userPasswordField;

    @FXML
    private void initialize() {

        validateOwnerPasswordField();
    }

    private void validateOwnerPasswordField() {

        switchCheckBoxes(true);
        ownerPasswordField.textProperty().addListener((observable, oldValue, newValue) -> switchCheckBoxes(newValue.isBlank()));
    }

    private void switchCheckBoxes(boolean flag) {

        isFillable.setDisable(flag);
        canManipulate.setDisable(flag);
        modifiable.setDisable(flag);
        printable.setDisable(flag);
        extractable.setDisable(flag);
    }

    private int getKeyLength() {

        if (lowRadio.isSelected())
            return 40;

        else if (aesRadio.isSelected())
            return 128;

        else if (highRadio.isSelected())
            return 256;

        else
            return 128;
    }

    private EncryptionData getEncryptionData(IntegerProperty integerProperty) {

        String ownerPassword = ownerPasswordField.getText();
        String userPassword = userPasswordField.getText();
        int keyLength = getKeyLength();
        boolean canExtract = extractable.isSelected();
        boolean canPrint = printable.isSelected();
        boolean canFill = isFillable.isSelected();
        boolean canModify = modifiable.isSelected();
        boolean canAssembleDoc = canManipulate.isSelected();

        if (!isFillable.isDisabled())
            return new EncryptionData(ownerPassword, userPassword, keyLength, canExtract, canPrint, canFill, canModify, canAssembleDoc, integerProperty);
        else
            return new EncryptionData(ownerPassword, userPassword, keyLength, true, true, true, true, true, integerProperty);
    }

    @FXML
    private void handleEncryptButton() {

        if (userPasswordField.getText().isBlank()) {
            CustomAlerts.alert("Encryption error", "Password is blank", "Enter strong password", ERROR);
            return;
        }

        String filename;
        try {

            filename = FilePicker.askToSaveFile(extractable, "Zip Files", "zip").getAbsolutePath();
            if (filename.isBlank())
                return;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<File> fileList = FilesManagement.getFileListByType(FileType.PDF).orElseThrow();
        int total = fileList.size();
        var determinateProgressView = new DeterminateProgressView();
        determinateProgressView.TaskDescriptionProperty().set("out of ".concat(total + " files encrypted"));
        var progressBarProperty = determinateProgressView.ProgressProperty();
        var integerProperty = new SimpleIntegerProperty(0);
        var encryptionData = getEncryptionData(integerProperty);

        var executorService = SingletonExecutorService.getInstance();

        Task<Void> encryptionTask = new Task<>() {

            @Override
            protected Void call() throws IOException, InterruptedException {

                integerProperty.addListener((observableValue, number, t1) -> {

                    updateProgress(t1.longValue(), total);
                    updateMessage(String.valueOf(t1.intValue()));
                });
                new Encryption(fileList, filename, encryptionData).encrypt();
                return null;
            }
        };

        executorService.submit(encryptionTask);
        progressBarProperty.bind(encryptionTask.progressProperty());
        determinateProgressView.DynamicTextProperty().bind(encryptionTask.messageProperty());

        encryptionTask.setOnSucceeded(workerStateEvent -> {

            try {

                encryptionTask.get();
                determinateProgressView.finish();
                if (integerProperty.get() > 0)
                    new CompletedProgressView("Documents have been successfully encrypted", fileList.get(0).getParent());

            } catch (NullPointerException e) {

                e.printStackTrace();
                CustomAlerts.alert("Access Denied", "Invalid file/directory path", "Files may have been moved or deleted",
                        ERROR);
            } catch (ExecutionException | InterruptedException e) {

                e.printStackTrace();
                CustomAlerts.alert("Extraction Error", "Trouble extracting from file. try again",
                        StackTraceUtil.getStackTrace(e));
            } finally {
                FilesManagement.removeFileListByType(FileType.PDF);
                aesRadio.getScene().getWindow().hide();
            }
        });
    }
}
