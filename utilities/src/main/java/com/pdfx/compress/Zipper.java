package com.pdfx.compress;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;

public class Zipper {

    private static void zipper(int level, ZipOutputStream zipOutputStream, File file) throws IOException {

        try (var bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {

            var zipEntry = new ZipEntry(file.getName());
            zipOutputStream.putNextEntry(zipEntry);
            zipOutputStream.setLevel(level);

            var bytes = new byte[16384];
            int length;
            while ((length = bufferedInputStream.read(bytes)) >= 0)
                zipOutputStream.write(bytes, 0, length);
        }
    }

    public static File zip(List<File> fileList, String filename, int level) throws IOException {

        try (var zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(filename)))) {

            for (var file : fileList)
                zipper(level, zipOutputStream, file);
        }
        return new File(filename);
    }

    public static File zip(File file, String filename, int level) throws IOException {

        String directory = file.getParent();
        File newFile = new File(directory, filename);
        FileUtils.touch(newFile);

        try (var zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(newFile)))) {

            zipper(level, zipOutputStream, file);
        }

        return newFile;
    }

    public static File unzip(File file) throws IOException {

        String directory = file.getParent();
        File newFile = null;

        final var buffer = new byte[16349];
        try (var zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            var zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null) {

                newFile = newFile(new File(directory), zipEntry);

                if (zipEntry.isDirectory()) {

                    if (!newFile.isDirectory() && !newFile.mkdirs())
                        throw new IOException("Failed to create directory " + newFile);

                } else {

                    // fix for Windows-created archives
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs())
                        throw new IOException("Failed to create directory " + parent);

                    // write file content
                    try (final var bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(newFile))) {

                        int len;
                        while ((len = zipInputStream.read(buffer)) > 0)
                            bufferedOutputStream.write(buffer, 0, len);
                    }
                }
                zipEntry = zipInputStream.getNextEntry();
            }
        }
        return newFile;
    }

    private static File newFile(File destinationDirectory, ZipEntry zipEntry) throws IOException {

        File destinationFile = new File(destinationDirectory, zipEntry.getName());

        String destinationDirectoryCanonicalPath = destinationDirectory.getCanonicalPath();
        String destinationFileCanonicalPath = destinationFile.getCanonicalPath();

        if (!destinationFileCanonicalPath.startsWith(destinationDirectoryCanonicalPath + File.separator))
            throw new IOException("Entry is outside of the target directory: " + zipEntry.getName());

        return destinationFile;
    }

    public static void zipDirectory(Path sourceFolderPath, File destinationZipPath) throws IOException {

        try (var zipOutputStream =
                     new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destinationZipPath)))) {

            Files.walkFileTree(sourceFolderPath, new SimpleFileVisitor<>() {

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                    zipOutputStream.putNextEntry(new ZipEntry(sourceFolderPath.relativize(file).toString()));
                    Files.copy(file, zipOutputStream);
                    zipOutputStream.closeEntry();
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }
}
