package com.pdfx.pdf2image;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.pdfutil.PdfFileUtil;
import com.pdfx.thread.SingletonExecutorService;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javafx.beans.property.IntegerProperty;
import javax.imageio.ImageIO;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class Pdf2Image {

    private final List<File> fileList;
    private final String outputFormat;
    private final int dpi;
    private final IntegerProperty integerProperty;

    public Pdf2Image(Pdf2ImageFiles pdf2ImageFiles) {

        this.integerProperty = pdf2ImageFiles.integerProperty();
        this.fileList = pdf2ImageFiles.fileList();
        this.dpi = pdf2ImageFiles.dpi();
        this.outputFormat = pdf2ImageFiles.outputFormat();
    }

    /**
     * Converts pdf to jpeg, jpg, png and saves the image
     *
     * @return true if operation ends successfully
     */
    public boolean convert() throws IOException, InterruptedException, ExecutionException, EncryptedFileException {

        int numberOfFiles = fileList.size();
        List<Callable<Void>> callableList = new ArrayList<>(numberOfFiles);

        var executorService = SingletonExecutorService.getInstance();

        for (int i = 0; i < numberOfFiles; i++) {

            int temp = i;

            Callable<Void> callable = () -> {

                final var originalPdf = fileList.get(temp);
                integerProperty.set(temp + 1);

                try (final var document = PDDocument.load(originalPdf)) {

                    if (document.isEncrypted())
                        throw new EncryptedFileException("Encrypted file(s) found.");

                    final var pdfRenderer = new PDFRenderer(document);

                    final int numberOfPdfPages = document.getNumberOfPages();

                    for (int page = 0; page < numberOfPdfPages; ++page) {

                        var bufferedImage = pdfRenderer.renderImageWithDPI(page, dpi, ImageType.RGB);

                        String suffix = String.valueOf(page + 1);
                        var newFileName = PdfFileUtil.getModifiedFileName(originalPdf, suffix);
                        var directory = PdfFileUtil.createDirectoryWithFileName(originalPdf);
                        var file = new File(directory, newFileName);
                        ImageIO.write(bufferedImage, outputFormat, file);
                        document.save(file);
                    }

                }
                return null;
            };
            callableList.add(callable);
        }
        executorService.invokeAll(callableList);
        return true;
    }
}
