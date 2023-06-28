package com.pdfx;

import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.view.ViewManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import static com.pdfx.view.View.HOME;

public class Main extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws IOException {

        var viewManager = ViewManager.getInstance(stage);
        viewManager.loadNewView("/fxml/Homepage.fxml", HOME);
        viewManager.showView(HOME, "PdfOps", true, false);

        stage.setOnCloseRequest(windowEvent -> {

            SingletonExecutorService.close();
            Platform.exit();
        });
    }
}