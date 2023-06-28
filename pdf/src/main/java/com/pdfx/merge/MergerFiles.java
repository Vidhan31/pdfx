package com.pdfx.merge;

import java.io.File;
import java.util.List;

public record MergerFiles(List<File> fileList, List<Integer> order, String fileName) {}
