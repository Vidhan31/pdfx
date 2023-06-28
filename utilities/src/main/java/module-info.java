module utilities {

    requires java.prefs;
    requires org.apache.commons.io;
    requires java.management;

    exports com.pdfx.fileshandler to pdf, ui;
    exports com.pdfx.exceptions to pdf, ui;
    exports com.pdfx.thread to ui, pdf;
    exports com.pdfx.compress to ui, pdf;
    exports com.pdfx.prefs;
    exports com.pdfx.logging;
}