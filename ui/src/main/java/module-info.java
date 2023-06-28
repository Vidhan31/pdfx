module ui {

    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires pdf;
    requires utilities;
    requires java.desktop;
    requires org.kordamp.ikonli.fluentui;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;

    exports com.pdfx to javafx.controls, javafx.graphics, javafx.fxml;
    exports com.pdfx.view to javafx.controls, javafx.graphics, javafx.fxml;

    opens com.pdfx.home to javafx.controls, javafx.graphics, javafx.fxml;
    opens com.pdfx.pdf.pdf2Image to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.util to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.pdf.split to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.pdf.merge to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.pdf.metadata to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.pdf.compress to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.pdf.extract2text to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.home.progress to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.pdf.encrypt to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.pdf.decrypt to javafx.controls, javafx.fxml, javafx.graphics;
    opens com.pdfx.view to javafx.controls, javafx.fxml, javafx.graphics;
}