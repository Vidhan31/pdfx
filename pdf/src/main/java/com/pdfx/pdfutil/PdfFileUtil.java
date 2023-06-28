package com.pdfx.pdfutil;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class PdfFileUtil {

    /**
     * Force creates parent directories if not already exists along with pwd having file name
     *
     * @param file name of file of which directory is to be created
     *
     * @return new directory path
     */
    public static String createDirectoryWithFileName(File file) throws IOException {

        String filePathWithoutExtension = FilenameUtils.removeExtension(file.getAbsolutePath());
        FileUtils.forceMkdir(new File(filePathWithoutExtension));
        return filePathWithoutExtension;
    }

    /**
     * @param file   the name to be modified
     * @param suffix custom information to attach with file name
     *
     * @return new modified file name
     */
    public static String getModifiedFileName(File file, String suffix) {

        return file.getName().replace(".pdf", "[" + suffix + "].pdf");
    }
}
