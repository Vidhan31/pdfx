package com.pdfx.extract.images;

import com.pdfx.exceptions.AccessPermissionsException;
import com.pdfx.thread.SingletonExecutorService;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.cos.COSStream;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceGray;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.color.PDPattern;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDAbstractPattern;
import org.apache.pdfbox.pdmodel.graphics.pattern.PDTilingPattern;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.pdmodel.graphics.state.PDSoftMask;
import org.apache.pdfbox.pdmodel.graphics.state.RenderingMode;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;

public class ImageExtraction {

    private final List<String> JPEG;
    private final File destinationPath;
    private final boolean useDirectJPEG;
    private final boolean noColorConvert;
    private final List<File> fileList;
    private final List<File> imagesList;

    private final Set<COSStream> seen;

    public ImageExtraction(ImageExtractionFiles imageExtractionFiles) {

        this.fileList = imageExtractionFiles.fileList();
        this.destinationPath = imageExtractionFiles.destinationPath();
        this.useDirectJPEG = imageExtractionFiles.useDirectJPEG();
        this.noColorConvert = imageExtractionFiles.noColorConvert();
        this.seen = new HashSet<>();
        this.JPEG = Arrays.asList(COSName.DCT_DECODE.getName(), COSName.DCT_DECODE_ABBREVIATION.getName());
        this.imagesList = new ArrayList<>();
    }

    public List<File> extract() throws AccessPermissionsException, IOException, InterruptedException {

        List<Callable<Void>> callableList = new ArrayList<>(fileList.size());
        var executorService = SingletonExecutorService.getInstance();

        for (var file : fileList) {

            Callable<Void> callable = () -> {

                try (var document = PDDocument.load(file)) {

                    Thread.currentThread().setName(file.getName());
                    var accessPermission = document.getCurrentAccessPermission();

                    if (!accessPermission.canExtractContent())
                        throw new AccessPermissionsException();

                    String prefix = FilenameUtils.removeExtension(file.getAbsolutePath());

                    for (int i = 0; i < document.getNumberOfPages(); i++)
                        new ImageGraphicsEngine(document.getPage(i), i, prefix).run();

                }
                return null;
            };
            callableList.add(callable);
        }
        executorService.invokeAll(callableList);
        return imagesList;
    }

    private class ImageGraphicsEngine extends PDFGraphicsStreamEngine {

        private final int pageNumber;
        private final String prefix;
        private int counter;

        protected ImageGraphicsEngine(PDPage page, int pageNumber, String prefix) {

            super(page);
            this.pageNumber = pageNumber + 1;
            counter = 1;
            this.prefix = prefix;
        }

        public void run() throws IOException {

            PDPage page = getPage();
            processPage(page);
            PDResources res = page.getResources();

            if (res == null)
                return;

            for (COSName name : res.getExtGStateNames()) {
                PDExtendedGraphicsState extGState = res.getExtGState(name);

                if (extGState == null) {
                    // can happen if key exists but no value
                    continue;
                }

                PDSoftMask softMask = extGState.getSoftMask();
                if (softMask != null) {
                    PDTransparencyGroup group = softMask.getGroup();
                    if (group != null) {

                        res.getExtGState(name).copyIntoGraphicsState(getGraphicsState());
                        processSoftMask(group);
                    }
                }
            }
        }

        @Override
        public void drawImage(PDImage pdImage) throws IOException {

            if (pdImage instanceof PDImageXObject xobject) {

                if (pdImage.isStencil())
                    processColor(getGraphicsState().getNonStrokingColor());

                try {
                    if (!seen.add(xobject.getCOSObject()))
                        return; // skip duplicate image
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }

            // save image
            String name = prefix + " (" + pageNumber + " - " + counter + ")";
            counter++;

            File file = write2file(pdImage, name, useDirectJPEG, noColorConvert);
            if (file != null)
                imagesList.add(file);
        }

        /**
         * Writes the image to a file with the filename prefix + an appropriate suffix, like "Image.jpg". The suffix is
         * automatically set depending on the image compression in the PDF.
         *
         * @param pdImage        the image.
         * @param prefix         the filename prefix.
         * @param directJPEG     if true, force saving JPEG/JPX streams as they are in the PDF file.
         * @param noColorConvert if true, images are extracted with their original colorspace if possible.
         *
         * @throws IOException When something is wrong with the corresponding file.
         */
        private File write2file(PDImage pdImage, String prefix, boolean directJPEG, boolean noColorConvert) throws IOException {

            /*

            In the case of noColorConvert being true, the method checks if the pdImage has a raw image (without color
            conversion). If a raw image is present, it is written to a file, and the corresponding File object is
            returned.

            For certain image formats such as "tiff" and a specific color space (PDDeviceGray.INSTANCE), there is a
            special handling to create a bitonal image and write it to a file. In this case, the File object is returned
            after writing the image.

            In all other cases, the image is written to a file using ImageIO.write, and the corresponding File object
            is returned.

             */
            String suffix = pdImage.getSuffix();

            if (suffix == null || "jb2".equals(suffix))
                suffix = "png";

            else if ("jpx".equals(suffix))
                suffix = "jp2";

            if (hasMasks(pdImage))
                suffix = "png";

            if (noColorConvert) {

                BufferedImage image = pdImage.getRawImage();

                if (image != null) {

                    int elements = image.getRaster().getNumDataElements();

                    suffix = "png";
                    if (elements > 3) {
                        suffix = "tiff";
                    }

                    File outputFile = new File(prefix + "." + suffix);
                    try (var bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                        ImageIO.write(image, suffix, bufferedOutputStream);
                        return outputFile;
                    }
                }
            }

            File outputFile = new File(prefix + "." + suffix);

            try (var bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile))) {

                if ("jpg".equals(suffix)) {

                    String colorSpaceName = pdImage.getColorSpace().getName();

                    if (directJPEG ||
                            (PDDeviceGray.INSTANCE.getName().equals(colorSpaceName) ||
                                    PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName))) {

                        InputStream data = pdImage.createInputStream(JPEG);
                        IOUtils.copy(data, bufferedOutputStream);
                        IOUtils.closeQuietly(data);

                    } else {

                        BufferedImage image = pdImage.getImage();
                        if (image != null)
                            ImageIO.write(image, suffix, bufferedOutputStream);
                    }

                } else if ("jp2".equals(suffix)) {

                    String colorSpaceName = pdImage.getColorSpace().getName();

                    if (directJPEG ||
                            (PDDeviceGray.INSTANCE.getName().equals(colorSpaceName) ||
                                    PDDeviceRGB.INSTANCE.getName().equals(colorSpaceName))) {

                        InputStream data =
                                pdImage.createInputStream(Collections.singletonList(COSName.JPX_DECODE.getName()));
                        IOUtils.copy(data, bufferedOutputStream);
                        IOUtils.closeQuietly(data);

                    } else {
                        BufferedImage image = pdImage.getImage();

                        if (image != null)
                            ImageIO.write(image, "jpeg2000", bufferedOutputStream);
                    }

                } else if ("tiff".equals(suffix) && pdImage.getColorSpace().equals(PDDeviceGray.INSTANCE)) {

                    BufferedImage image = pdImage.getImage();
                    if (image == null)
                        return null;

                    int w = image.getWidth();
                    int h = image.getHeight();
                    var bitonalImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
                    for (int y = 0; y < h; y++) {

                        for (int x = 0; x < w; x++) {

                            bitonalImage.setRGB(x, y, image.getRGB(x, y));
                        }
                    }

                    ImageIO.write(bitonalImage, suffix, bufferedOutputStream);
                    String baseName = FilenameUtils.getBaseName(outputFile.getAbsolutePath());
                    FileUtils.forceMkdirParent(new File(destinationPath.getAbsolutePath() + File.separator + baseName));
                    FileUtils.moveFileToDirectory(outputFile, new File(baseName), true);
                    return outputFile;

                } else {

                    BufferedImage image = pdImage.getImage();

                    if (image != null)
                        ImageIO.write(image, suffix, bufferedOutputStream);

                    return outputFile;
                }
            }
            return outputFile;
        }

        private boolean hasMasks(PDImage pdImage) throws IOException {

            if (pdImage instanceof PDImageXObject ximg)
                return ximg.getMask() != null || ximg.getSoftMask() != null;

            return false;
        }

        @Override
        protected void showGlyph(Matrix textRenderingMatrix, PDFont font, int code, Vector displacement) throws IOException {

            PDGraphicsState graphicsState = getGraphicsState();
            RenderingMode renderingMode = graphicsState.getTextState().getRenderingMode();

            if (renderingMode.isFill())
                processColor(graphicsState.getNonStrokingColor());

            if (renderingMode.isStroke())
                processColor(graphicsState.getStrokingColor());
        }

        @Override
        public void strokePath() throws IOException {
            processColor(getGraphicsState().getStrokingColor());
        }

        @Override
        public void fillPath(int windingRule) throws IOException {
            processColor(getGraphicsState().getNonStrokingColor());
        }

        @Override
        public void fillAndStrokePath(int windingRule) throws IOException {
            processColor(getGraphicsState().getNonStrokingColor());
        }

        @Override
        public void shadingFill(COSName shadingName) {
            // Empty: add special handling if needed
        }

        // find out if it is a tiling pattern, then process that one
        private void processColor(PDColor color) throws IOException {

            if (color.getColorSpace() instanceof PDPattern pattern) {

                PDAbstractPattern abstractPattern = pattern.getPattern(color);

                if (abstractPattern instanceof PDTilingPattern)
                    processTilingPattern((PDTilingPattern) abstractPattern, null, null);
            }
        }

        @Override
        public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) {
            // Empty: add special handling if needed
        }

        @Override
        public void clip(int windingRule) {
            // Empty: add special handling if needed
        }

        @Override
        public void moveTo(float x, float y) {
            // Empty: add special handling if needed
        }

        @Override
        public void lineTo(float x, float y) {
            // Empty: add special handling if needed
        }

        @Override
        public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) {
            // Empty: add special handling if needed
        }

        @Override
        public Point2D getCurrentPoint() {
            return new Point2D.Float(0, 0);
        }

        @Override
        public void closePath() {
            // Empty: add special handling if needed
        }

        @Override
        public void endPath() {
            // Empty: add special handling if needed
        }
    }
}
