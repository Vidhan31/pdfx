package com.pdfx.fileshandler;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Centralized class for managing user selected files
 */
public class FilesManagement {

    private static HashMap<FileType, List<File>> files;

    public FilesManagement() {

        if (files == null)
            FilesManagement.files = new HashMap<>();
    }

    public static void removeFileListByType(FileType fileType) {

        files.remove(fileType);
    }

    public static boolean containsFileType(FileType fileType) {

        return files != null && files.containsKey(fileType);
    }

    public static int getTotalFilesNumber(FileType fileType) {

        if (files != null) {

            if (getFileListByType(fileType).isPresent())
                return getFileListByType(fileType).get().size();
            else
                return 0;
        }
        return 0;
    }

    public static Optional<List<File>> getFileListByType(FileType fileType) {

        return Optional.ofNullable(files.get(fileType));
    }

    /**
     * Add new files or replace if files of requested file type already exists
     *
     * @param fileType type of the file
     * @param fileList list of files
     */
    public void addFiles(FileType fileType, List<File> fileList) {

        if (files.containsKey(fileType))
            files.replace(fileType, fileList);
        else
            files.put(fileType, fileList);
    }
}
