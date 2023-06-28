package com.pdfx.util;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

public class FileManagerUtil {

    public static void openPathInWindows(String path) {
        try {
            Runtime.getRuntime().exec("explorer" + " " + path);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open specified file/folder in explorer");
        }
    }

    public static void openInDefaultFileManager(String path) {

        if (System.getProperty("os.name").toLowerCase().contains("win"))
            openPathInWindows(path);
        else
            Desktop.getDesktop().browseFileDirectory(new File(path));
    }
}
