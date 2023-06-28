package com.pdfx.extract.text;

import com.pdfx.pdfutil.PdfFileUtil;
import com.pdfx.thread.SingletonExecutorService;
import io.github.jonathanlink.PDFLayoutTextStripper;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class Extract2Text {

    public static List<String> extractBulk(List<File> fileList) throws InterruptedException, ExecutionException {

        var executorService = SingletonExecutorService.getInstance();
        List<Callable<String>> callableList = new ArrayList<>(fileList.size());
        List<String> textList = new ArrayList<>(fileList.size());

        for (var file : fileList) {

            Callable<String> callable = () -> {

                PDFParser pdfParser = new PDFParser(new RandomAccessFile(file, "r"));
                pdfParser.parse();
                try (PDDocument pdDocument = new PDDocument(pdfParser.getDocument())) {

                    PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
                    return pdfTextStripper.getText(pdDocument);
                }
            };
            callableList.add(callable);
        }

        var futureList = executorService.invokeAll(callableList);
        for (var future : futureList)
            textList.add(future.get());

        return textList;
    }

    public static String extractToFile(File file, String filename) throws IOException {

        File newFile = new File(PdfFileUtil.createDirectoryWithFileName(file), filename);
        FileUtils.touch(newFile);
        String output;
        PDFParser pdfParser = new PDFParser(new RandomAccessFile(file, "r"));
        pdfParser.parse();

        try (PDDocument pdDocument = new PDDocument(pdfParser.getDocument())) {

            PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();

            try (var bufferedWriter = new BufferedWriter(new FileWriter(file))) {

                output = pdfTextStripper.getText(pdDocument);
                bufferedWriter.write(output);
            }
        }
        return output;
    }

    public static String extract(File file) throws IOException {

        PDFParser pdfParser = new PDFParser(new RandomAccessFile(file, "r"));
        pdfParser.parse();

        try (PDDocument pdDocument = new PDDocument(pdfParser.getDocument())) {

            PDFTextStripper pdfTextStripper = new PDFLayoutTextStripper();
            String extraced = pdfTextStripper.getText(pdDocument);
            return extraced;
        }
    }
}
