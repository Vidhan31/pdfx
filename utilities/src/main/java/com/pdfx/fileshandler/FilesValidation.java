package com.pdfx.fileshandler;

import com.pdfx.thread.SingletonExecutorService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FilenameUtils;

import static com.pdfx.fileshandler.FileType.NONE;
import static com.pdfx.fileshandler.FileType.PDF;

public class FilesValidation {

    private final List<File> selectedFiles;
    private final FileType fileType;

    /**
     * Constructs a new instance of {@code FilesValidation} using the specified list of selected files and tab name.
     *
     * @param selectedFiles the list of selected files to validate
     * @param tabName       the name of the tab where the files were selected
     */
    public FilesValidation(List<File> selectedFiles, String tabName) {

        this.selectedFiles = selectedFiles;
        this.fileType = getFileType(tabName);
    }

    /**
     * Determines the type of file based on the specified tab name.
     *
     * @return the type of file based on the specified tab name
     */
    public FileType getFileType(String tabName) {

        return switch (tabName) {

            case "pdf" -> PDF;
            default -> NONE;
        };
    }

    /**
     * Accepts the selected files if they are valid and adds them to HashMap with {@link FileType} as key and
     * {@code List<File>} as value.
     *
     * @return {@code true} if the selected files are accepted and added to the list, {@code false} otherwise
     */
    public boolean acceptFiles() throws ExecutionException, InterruptedException {

        if (isSelectFileListValid()) {

            new FilesManagement().addFiles(fileType, selectedFiles);
            return true;
        }
        return false;
    }

    /**
     * Checks if the selected file list is valid by ensuring that all files have the same extension as the specified tab
     * name.
     *
     * @return {@code true} if the selected file list is valid, {@code false} otherwise
     */
    public boolean isSelectFileListValid() throws InterruptedException, ExecutionException {

        if (selectedFiles.isEmpty())
            return false;

        List<Callable<Boolean>> callables = new ArrayList<>();
        var executorService = SingletonExecutorService.getInstance();

        for (var file : selectedFiles) {

            Callable<Boolean> callable =
                    () -> (getFileType(FilenameUtils.getExtension(file.getName())).equals(fileType));
            callables.add(callable);
        }

        var futureList = executorService.invokeAll(callables);

        for (var booleanFuture : futureList) {

            if (!booleanFuture.get())
                return false;
        }

        return true;
    }
}
