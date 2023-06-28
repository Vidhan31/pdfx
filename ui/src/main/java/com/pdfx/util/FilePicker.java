package com.pdfx.util;

import com.pdfx.fileshandler.FilesValidation;
import com.pdfx.prefs.PreferencesUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;
import javafx.scene.Node;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class FilePicker {

    public static void chooseFiles(Node owner) throws ExecutionException, InterruptedException {

        var fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file");
        String getLastUsedDirectoryPath = PreferencesUtil.getLastUsedDirectoryPath();
        fileChooser.setInitialDirectory(new File(getLastUsedDirectoryPath));
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Portable Document Format (PDF) files", "*.pdf"));
        var selectedFiles = fileChooser.showOpenMultipleDialog(owner.getScene().getWindow());

        if (selectedFiles != null) {

            if (new FilesValidation(selectedFiles, "pdf").acceptFiles())
                PreferencesUtil.setLastUsedDirectoryPath(selectedFiles.get(0).getParent());
        }
    }

    public static File askToSaveFile(Node node, String description, String extensions) throws FileNotFoundException {

        FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, "*.".concat(extensions));
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showSaveDialog(node.getScene().getWindow());
        if (file == null)
            throw new FileNotFoundException();

        return file;
    }

    public static File chooseDirectory(Node node) throws FileNotFoundException {

        var directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File(PreferencesUtil.getLastUsedDirectoryPath()));
        directoryChooser.setTitle("Select a folder");
        File file = directoryChooser.showDialog(node.getScene().getWindow());
        if (file == null)
            throw new FileNotFoundException();

        return file;
    }
}
