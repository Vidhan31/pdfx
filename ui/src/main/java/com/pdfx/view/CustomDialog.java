package com.pdfx.view;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CustomDialog extends Dialog<String> {

    /**
     * Constructs a new instance of {@code CustomDialog} using the specified FXML resource and owner stage. Loads the
     * FXML file and sets the dialog pane content to the loaded view. Also sets the owner and modality of the dialog.
     *
     * @param fxml  the path to the FXML file to load
     * @param owner the owner {@code Stage} of the dialog
     *
     * @throws IOException if an error occurs while loading the FXML file
     */

    public CustomDialog(String fxml, Stage owner) throws IOException {

        initOwner(owner);
        initModality(Modality.WINDOW_MODAL);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxml));
        DialogPane dialogPane = fxmlLoader.load();

        getDialogPane().getStylesheets().add(getClass().getResource("/fxml/css/nord-dark.css").toExternalForm());
        getDialogPane().setContent(dialogPane);
        getDialogPane().setPadding(Insets.EMPTY);
    }
}
