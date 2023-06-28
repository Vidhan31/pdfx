package com.pdfx.metadata;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;

public class MetadataEditor {

    private final File file;
    private final String title;
    private final String author;
    private final String subject;
    private final String creator;
    private final String producer;
    private final String keywords;
    private final Calendar creation;
    private final Calendar modification;
    private final PDDocumentInformation information;

    public MetadataEditor(Metadata metadata) {

        this.file = metadata.file();
        this.title = metadata.title();
        this.author = metadata.author();
        this.subject = metadata.subject();
        this.creator = metadata.creator();
        this.producer = metadata.producer();
        this.keywords = metadata.keywords();
        this.creation = metadata.creation();
        this.modification = metadata.modification();
        information = new PDDocumentInformation();
    }

    public void edit() throws IOException {

        information.setTitle(title);
        information.setAuthor(author);
        information.setSubject(subject);
        information.setCreator(creator);
        information.setProducer(producer);
        information.setKeywords(keywords);
        information.setCreationDate(creation);
        information.setModificationDate(modification);

        try (PDDocument pdDocument = PDDocument.load(file)) {
            pdDocument.setDocumentInformation(information);
            pdDocument.save(file);
        }
    }
}
