package com.pdfx.metadata;

import java.io.File;
import java.util.Calendar;

public record Metadata(File file, String title, String author, String subject, String creator, String producer, String keywords,
                       Calendar creation, Calendar modification) {}
