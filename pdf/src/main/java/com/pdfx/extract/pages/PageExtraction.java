package com.pdfx.extract.pages;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.exceptions.FileNotSelectedException;
import com.pdfx.pdfutil.PdfFileUtil;
import com.pdfx.thread.SingletonExecutorService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import javafx.beans.property.StringProperty;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PageExtraction {

    private final File originalFile;
    private final Optional<List<Integer>> optionalRange;
    private final StringProperty stringProperty;

    public PageExtraction(PageExtractionFiles pageExtractionFiles) {

        this.originalFile = pageExtractionFiles.file();
        this.optionalRange = pageExtractionFiles.range();
        this.stringProperty = pageExtractionFiles.stringProperty();
    }

    /**
     * Extracts all pages from pdf individually
     *
     * @return true if operations ends successfully
     */
    public boolean extractAll() throws IOException, EncryptedFileException, InterruptedException {

        try (var document = PDDocument.load(originalFile)) {

            if (document.isEncrypted())
                throw new EncryptedFileException();

            int total = document.getNumberOfPages();

            var executorService = SingletonExecutorService.getInstance();
            List<Callable<Void>> callableList = new ArrayList<>(total);

            for (int i = 0; i < total; i++) {

                int finalI = i;
                Callable<Void> callable = () -> {

                    try (var pdDocument = new PDDocument()) {

                        PDPage page = document.getPage(finalI);
                        pdDocument.addPage(page);
                        String directory = PdfFileUtil.createDirectoryWithFileName(originalFile);
                        String fileName = PdfFileUtil.getModifiedFileName(originalFile, String.valueOf(finalI + 1));
                        pdDocument.save(new File(directory, fileName));
                        stringProperty.set("Page " + finalI);
                    }
                    return null;
                };
                callableList.add(callable);
            }
            executorService.invokeAll(callableList);
        }
        return true;
    }

    /**
     * Extracts pages from give range
     *
     * @return true if operation ends successfully
     */
    public boolean extract() throws InterruptedException, IOException, EncryptedFileException,
            FileNotSelectedException {

        var range = optionalRange.orElseThrow(FileNotSelectedException::new);

        var executorService = SingletonExecutorService.getInstance();
        List<Callable<Void>> callableList = new ArrayList<>(range.size());

        for (int i = 0; i < range.size(); i++) {

            int finalI = i;
            Callable<Void> callable = () -> {

                extractPage(range.get(finalI));
                return null;
            };
            callableList.add(callable);
        }

        executorService.invokeAll(callableList);
        return true;
    }

    /**
     * Extract page from the pdf
     *
     * @param page page to be extracted
     */
    private void extractPage(int page) throws IOException, EncryptedFileException {

        stringProperty.set("Page " + page);
        try (var document = PDDocument.load(originalFile)) {

            if (document.isEncrypted())
                throw new EncryptedFileException();

            var splitter = new Splitter();
            splitter.setStartPage(page);
            splitter.setEndPage(page);
            splitter.setSplitAtPage(page);
            var splittedDocuments = splitter.split(document);

            String directory = PdfFileUtil.createDirectoryWithFileName(originalFile);
            String fileName = PdfFileUtil.getModifiedFileName(originalFile, String.valueOf(page));
            for (var documents : splittedDocuments)
                documents.save(new File(directory, fileName));
        }
    }
}
