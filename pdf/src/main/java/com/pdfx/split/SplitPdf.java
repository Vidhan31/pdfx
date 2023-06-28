package com.pdfx.split;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.pdfutil.PdfFileUtil;
import com.pdfx.thread.SingletonExecutorService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.beans.property.StringProperty;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

public class SplitPdf {

    private final File originalFile;
    private final List<Integer> range;
    private final StringProperty stringProperty;

    public SplitPdf(SplitPdfFiles pdfFiles) {

        this.originalFile = pdfFiles.file();
        this.range = pdfFiles.range().stream().sorted().toList();
        this.stringProperty = pdfFiles.stringProperty();
    }

    /**
     * Splits specified pdf pages
     *
     * @return true if operation ends successfully
     */
    public boolean split() throws InterruptedException, IOException, EncryptedFileException {

        var fileRanges = getPageToBeSplittedFrom();

        var executorService = SingletonExecutorService.getInstance();
        List<Callable<Void>> callableList = new ArrayList<>(fileRanges.size());

        for (int i = 0; i < fileRanges.size(); i++) {

            int finalI = i;
            Callable<Void> callable = () -> {

                splitRanges(fileRanges.get(finalI));
                stringProperty.set("Page " + finalI);
                return null;
            };
            callableList.add(callable);
        }

        executorService.invokeAll(callableList);
        return true;
    }

    /**
     * Creates a nested list which contains different list of page numbers to be splitted
     *
     * @return nested list of range of numbers
     */
    private List<List<Integer>> getPageToBeSplittedFrom() {

        List<List<Integer>> splitList = new ArrayList<>();

        int start = 0;
        int end;
        for (int i = 1; i < range.size(); i++) {
            if (range.get(i) - range.get(i - 1) != 1) {
                end = i - 1;
                splitList.add(range.subList(start, end + 1));
                start = i;
            }
        }
        splitList.add(range.subList(start, range.size()));
        return splitList;
    }

    /**
     * Splits the given range of pdf page numbers
     *
     * @param pages range of pdf page numbers to be splitted
     */
    private void splitRanges(List<Integer> pages) throws IOException, EncryptedFileException {

        try (var document = PDDocument.load(originalFile)) {

            if (document.isEncrypted())
                throw new EncryptedFileException();

            int start = pages.get(0);
            int end = pages.get(pages.size() - 1);

            var splitter = new Splitter();
            splitter.setStartPage(start);
            splitter.setEndPage(end);
            splitter.setSplitAtPage(end);

            var splittedDocuments = splitter.split(document);

            String directory = PdfFileUtil.createDirectoryWithFileName(originalFile);
            String fileName = PdfFileUtil.getModifiedFileName(originalFile, start + "-" + end);
            for (var documents : splittedDocuments)
                documents.save(new File(directory, fileName));
        }
    }
}
