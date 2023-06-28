package com.pdfx.metadata;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

public class MetadataRetriever {

    private final File file;

    public MetadataRetriever(File file) {

        this.file = file;
    }

    public Metadata retrieve() throws IOException {

        try (PDDocument document = PDDocument.load(file)) {

            PDDocumentInformation information = document.getDocumentInformation();
            final File file = this.file;
            final String title = information.getTitle();
            final String author = information.getAuthor();
            final String subject = information.getSubject();
            final String creator = information.getCreator();
            final String producer = information.getProducer();
            final String keywords = information.getKeywords();
            final Calendar creation = information.getCreationDate();
            final Calendar modification = information.getModificationDate();
            return new Metadata(file, title, author, subject, creator, producer, keywords, creation, modification);
        }
    }
}
