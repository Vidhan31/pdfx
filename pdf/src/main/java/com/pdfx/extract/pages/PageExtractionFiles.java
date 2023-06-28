package com.pdfx.extract.pages;

import java.io.File;
import java.util.List;
import java.util.Optional;
import javafx.beans.property.StringProperty;

public record PageExtractionFiles(File file, Optional<List<Integer>> range, StringProperty stringProperty) {}
