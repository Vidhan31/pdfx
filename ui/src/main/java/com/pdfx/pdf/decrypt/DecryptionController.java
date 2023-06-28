package com.pdfx.pdf.decrypt;

import com.pdfx.decrypt.Decrypt;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.home.progress.CompletedProgressView;
import com.pdfx.util.CustomAlerts;
import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;

public class DecryptionController {

    @FXML
    private PasswordField ownerPasswordField;

    @FXML
    private Button decryptButton;

    @FXML
    private void initialize() {

        ownerPasswordField.textProperty().addListener((observable, oldValue, newValue) -> decryptButton.setDisable(newValue.isBlank()));
    }

    @FXML
    private void handleDecryptButton() {

        File file = FilesManagement.getFileListByType(FileType.PDF).orElseThrow().get(0);

        try {
            Decrypt.decrypt(file, ownerPasswordField.getText());
        } catch (Exception e) {
            CustomAlerts.alert("Loading error", "Invalid password", "Enter a correct owner password", Alert.AlertType.ERROR);
            return;
        }
        new CompletedProgressView(file.getName() + "decrypted successfully", file.getParent());
        ownerPasswordField.getScene().getWindow().hide();
        FilesManagement.removeFileListByType(FileType.PDF);
    }
}
