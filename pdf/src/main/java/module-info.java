module pdf {

    requires utilities;
    requires org.apache.pdfbox;
    requires java.desktop;
    requires org.apache.commons.io;
    requires javafx.base;
    requires PDFLayoutTextStripper;

    exports com.pdfx.pdf2image to ui;
    exports com.pdfx.pdfutil to ui;
    exports com.pdfx.split to ui;
    exports com.pdfx.merge to ui;
    exports com.pdfx.metadata to ui;
    exports com.pdfx.extract.text to ui;
    exports com.pdfx.encrypt to ui;
    exports com.pdfx.decrypt to ui;
    exports com.pdfx.extract.images to ui;
    exports com.pdfx.extract.pages to ui;
}