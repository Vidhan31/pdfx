package com.pdfx.pdf.extract2text;

import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.extract.text.Extract2Text;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import com.pdfx.util.FilePicker;
import com.pdfx.view.ViewManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import static com.pdfx.view.View.EXTRACT;
import static com.pdfx.view.View.HOME;

public class Extract2TextController {

    @FXML
    private TextArea textArea;

    @FXML
    private ProgressIndicator extractIndicator;

    @FXML
    private void initialize() throws ExecutionException, InterruptedException {

        extractIndicator.setVisible(true);
        textArea.setText(displayText());
        extractIndicator.setVisible(false);
    }

    private String displayText() throws ExecutionException, InterruptedException {

        File file = FilesManagement.getFileListByType(FileType.PDF).orElseThrow().get(0);

        var future = SingletonExecutorService.getInstance().submit(() -> {

            try {
                return Extract2Text.extract(file);
            } catch (IOException e) {

                CustomAlerts.alert("File Parsing error", "Check if pdf does not contain only images", "Alternatively, check if" +
                        " it " +
                        "is well structured pdf containing texts", Alert.AlertType.ERROR);
                e.printStackTrace();
            }
            return null;
        });

        return future.get();
    }
    @FXML
    private void handleBackButtonClick() throws IOException {

        var viewManager = ViewManager.getInstance(null);
        viewManager.showView(HOME, "PDF Tools", true, false);
        ViewManager.removeView(EXTRACT);
    }

    @FXML
    private void handleSaveToTextFileButton() {

        extractIndicator.setVisible(true);
        File file;
        try {
            file = FilePicker.askToSaveFile(textArea, "Text Files", "txt");
        } catch (FileNotFoundException e) {
            CustomAlerts.alert("File not found error", "Create and save new file", null, Alert.AlertType.ERROR);
            extractIndicator.setVisible(false);
            return;
        }

        try (var bufferedWriter = new BufferedWriter(new FileWriter(file))) {

            bufferedWriter.write(textArea.getText());
        } catch (IOException e) {
            CustomAlerts.alert("File Parsing error", "Check if pdf does not contain only images", "Alternatively, check if it " +
                    "is well structured pdf containing texts", Alert.AlertType.ERROR);
            extractIndicator.setVisible(false);
            return;
        } finally {
            FilesManagement.removeFileListByType(FileType.PDF);
        }
        extractIndicator.setVisible(false);
    }

    @FXML
    private void handleCopyButton() {

        ClipboardContent content = new ClipboardContent();
        content.putString(textArea.getText());
        Clipboard.getSystemClipboard().setContent(content);
    }
}
