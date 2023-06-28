package com.pdfx.pdfutil;

import com.pdfx.exceptions.EncryptedFileException;
import com.pdfx.thread.SingletonExecutorService;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

public class PdfPreviews {

    private final List<File> originalPdfFiles;
    private final List<BufferedImage> listOfThumbNails;

    public PdfPreviews(List<File> originalFiles) {

        this.originalPdfFiles = originalFiles;
        this.listOfThumbNails = new ArrayList<>();
    }

    /**
     * Creates image thumbnails with specified size for multiple pdf documents
     *
     * @return list of {@code BufferedImage} images for previews
     */
    public List<BufferedImage> generateMultiplePDFPreviews(int width, int height) throws InterruptedException,
            EncryptedFileException, IOException, ExecutionException {

        List<Future<BufferedImage>> futurePreviews = new ArrayList<>(originalPdfFiles.size());
        List<BufferedImage> listOfThumbNails = new ArrayList<>();

        for (File originalPdfFile : originalPdfFiles) {

            CompletableFuture<BufferedImage> completableFuture = CompletableFuture.supplyAsync(() -> {

                try {
                    return generateSinglePDFPreview(originalPdfFile, width, height);
                } catch (EncryptedFileException | IOException e) {
                    e.printStackTrace();
                    return null;
                }

            }, SingletonExecutorService.getInstance());

            futurePreviews.add(completableFuture);
        }

        for (Future<BufferedImage> future : futurePreviews) {
            listOfThumbNails.add(future.get());
        }

        return listOfThumbNails;
    }

    /**
     * Generates thumbnail of first page of pdf files
     *
     * @return List of {@code BufferedImage} containing previews.
     */
    public static BufferedImage generateSinglePDFPreview(File file, int width, int height) throws EncryptedFileException,
            IOException {

        try (final var document = PDDocument.load(file)) {

            if (document.isEncrypted())
                throw new EncryptedFileException("Encrypted file(s) found.");

            final var pdfRenderer = new PDFRenderer(document);

            var bufferedImage = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
            var thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = thumbnail.createGraphics();
            graphics.drawImage(bufferedImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH), 0, 0, null);
            graphics.dispose();
            return thumbnail;
        }
    }

    /**
     * Creates image thumbnails with specified size for a single pdf document
     *
     * @return list of {@code BufferedImage} images for previews
     */
    public List<BufferedImage> generateAllPagesPDFPreviews(int width, int height) throws InterruptedException,
            ExecutionException, EncryptedFileException, IOException {

        try (final var document = PDDocument.load(originalPdfFiles.get(0))) {

            if (document.isEncrypted())
                throw new EncryptedFileException("Encrypted file(s) found.");

            final var pdfRenderer = new PDFRenderer(document);

            final int numberOfPdfPages = document.getNumberOfPages();

            for (int page = 0; page < numberOfPdfPages; ++page) {

                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
                BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D graphics = thumbnail.createGraphics();
                graphics.drawImage(bufferedImage.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH), 0, 0,
                        null);
                graphics.dispose();
                listOfThumbNails.add(thumbnail);
            }
        }
        return listOfThumbNails;
    }
}
