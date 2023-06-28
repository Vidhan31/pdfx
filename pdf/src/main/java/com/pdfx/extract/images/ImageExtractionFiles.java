package com.pdfx.extract.images;

import java.io.File;
import java.util.List;

public record ImageExtractionFiles(List<File> fileList, File destinationPath, boolean useDirectJPEG, boolean noColorConvert) {}
