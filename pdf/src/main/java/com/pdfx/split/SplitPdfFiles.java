package com.pdfx.split;

import java.io.File;
import java.util.List;
import javafx.beans.property.StringProperty;

public record SplitPdfFiles(File file, List<Integer> range, StringProperty stringProperty) {}
