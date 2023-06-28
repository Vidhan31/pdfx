package com.pdfx.encrypt;

import javafx.beans.property.IntegerProperty;

public record EncryptionData(String ownerPassword, String userPassword, int keyLength, boolean canExtract, boolean canPrint,
                             boolean canFill, boolean canModify, boolean canAssembleDocument, IntegerProperty integerProperty) {}
