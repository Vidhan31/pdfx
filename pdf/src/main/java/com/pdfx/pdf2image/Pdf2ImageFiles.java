package com.pdfx.pdf2image;

import java.io.File;
import java.util.List;
import javafx.beans.property.IntegerProperty;

public record Pdf2ImageFiles(List<File> fileList, int dpi, String outputFormat, IntegerProperty integerProperty) {}
