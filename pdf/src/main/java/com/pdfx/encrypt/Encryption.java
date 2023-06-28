package com.pdfx.encrypt;

import com.pdfx.compress.Zipper;
import com.pdfx.thread.SingletonExecutorService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.beans.property.IntegerProperty;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

public class Encryption {

    private final List<File> fileList;
    private final String filePath;
    private final String ownerPassword;
    private final String userPassword;
    private final int keyLength;
    private final boolean canExtract;
    private final boolean canPrint;
    private final boolean canFill;
    private final boolean canModify;
    private final boolean canAssembleDocument;
    private final IntegerProperty integerProperty;

    public Encryption(List<File> fileList, String filePath, EncryptionData encryptionData) {

        this.fileList = fileList;
        this.filePath = filePath;
        this.ownerPassword = encryptionData.ownerPassword();
        this.userPassword = encryptionData.userPassword();
        this.keyLength = encryptionData.keyLength();
        this.canExtract = encryptionData.canExtract();
        this.canPrint = encryptionData.canPrint();
        this.canFill = encryptionData.canFill();
        this.canModify = encryptionData.canModify();
        this.canAssembleDocument = encryptionData.canAssembleDocument();
        this.integerProperty = encryptionData.integerProperty();
    }

    public File encrypt() throws IOException, InterruptedException {

        final List<File> encryptedFiles = new ArrayList<>(fileList.size());
        final List<Callable<Void>> callableList = new ArrayList<>(fileList.size());
        final var executorService = SingletonExecutorService.getInstance();

        for (int i = 0; i < fileList.size(); i++) {

            int finalI = i;
            final Callable<Void> callable = () -> {

                try (final var document = PDDocument.load(fileList.get(finalI))) {

                    final var accessPermission = new AccessPermission();
                    accessPermission.setCanExtractContent(canExtract);
                    accessPermission.setCanPrint(canPrint);
                    accessPermission.setCanModifyAnnotations(canFill);
                    accessPermission.setCanFillInForm(canFill);
                    accessPermission.setCanModify(canModify);
                    accessPermission.setCanAssembleDocument(canAssembleDocument);

                    final var standardProtectionPolicy = new StandardProtectionPolicy(ownerPassword, userPassword,
                            accessPermission);
                    standardProtectionPolicy.setEncryptionKeyLength(keyLength);
                    standardProtectionPolicy.setPreferAES(true);
                    document.protect(standardProtectionPolicy);
                    File tempFile = File.createTempFile(fileList.get(finalI).getName().concat("-encrypted"), ".pdf");
                    document.save(tempFile);
                    encryptedFiles.add(tempFile);
                    tempFile.deleteOnExit();
                    integerProperty.set(finalI + 1);
                }
                return null;
            };
            callableList.add(callable);
        }

        executorService.invokeAll(callableList);
        return Zipper.zip(encryptedFiles, filePath, -1);
    }
}

