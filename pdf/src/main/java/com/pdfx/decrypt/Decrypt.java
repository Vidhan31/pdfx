package com.pdfx.decrypt;

import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;

public class Decrypt {

    public static File decrypt(File file, String ownerPassword) throws IOException {

        try (final var document = PDDocument.load(file, ownerPassword)) {

            if (document.isEncrypted()) {

                document.setAllSecurityToBeRemoved(true);
                document.save(file);
            }
        }
        return file;
    }
}
