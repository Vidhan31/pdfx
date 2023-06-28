package com.pdfx.util;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SelectionView {

    private final List<BufferedImage> bufferedImageList;
    private final List<String> selections;
    private final FlowPane flowPane;

    public SelectionView(FlowPane flowPane, List<BufferedImage> bufferedImageList) {

        this.flowPane = flowPane;
        this.bufferedImageList = bufferedImageList;
        initialize();
        this.selections = new LinkedList<>();
    }

    private void initialize() {

        for (int i = 0; i < bufferedImageList.size(); i++) {

            VBox root = new VBox();
            root.setMaxHeight(Double.NEGATIVE_INFINITY);
            root.setMaxWidth(Double.NEGATIVE_INFINITY);
            root.setMinHeight(Double.NEGATIVE_INFINITY);
            root.setMinWidth(Double.NEGATIVE_INFINITY);
            root.setPrefHeight(244.0);
            root.setPrefWidth(198.0);
            root.setStyle("-fx-border-color: white;");

            HBox stackHbox = new HBox();
            VBox.setVgrow(stackHbox, Priority.ALWAYS);

            StackPane stackPane = new StackPane();
            stackPane.setPrefHeight(150.0);
            stackPane.setPrefWidth(200.0);

            ImageView preview = convertToFxImage(bufferedImageList.get(i));
            preview.setFitHeight(240.0);
            preview.setFitWidth(200.0);
            preview.setPickOnBounds(true);
            preview.setPreserveRatio(true);

            HBox hBox = new HBox();
            CheckBox checkBox = new CheckBox();
            checkBox.setMnemonicParsing(false);
            HBox.setMargin(checkBox, new Insets(7.0, 0.0, 0.0, 10.0));
            hBox.getChildren().add(checkBox);

            stackPane.getChildren().addAll(preview, hBox);
            stackHbox.getChildren().add(stackPane);

            HBox labelHbox = new HBox();
            Region region1 = new Region();
            region1.setLayoutX(10.0);
            region1.setLayoutY(10.0);
            HBox.setHgrow(region1, Priority.ALWAYS);

            Label label = new Label(String.valueOf(i));
            HBox.setHgrow(label, Priority.ALWAYS);
            labelHbox.getChildren().addAll(region1, label);

            Region region2 = new Region();
            HBox.setHgrow(region2, Priority.ALWAYS);

            labelHbox.getChildren().add(region2);

            root.getChildren().addAll(stackHbox, labelHbox);

            flowPane.getChildren().add(root);

            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {

                if (newValue)
                    selections.add(label.getText());
                else
                    selections.remove(label.getText());

                System.out.println(selections);
            });
        }
    }

    public static ImageView convertToFxImage(BufferedImage image) {

        WritableImage wr = null;

        if (image != null) {

            wr = new WritableImage(image.getWidth(), image.getHeight());

            PixelWriter pw = wr.getPixelWriter();

            for (int x = 0; x < image.getWidth(); x++) {

                for (int y = 0; y < image.getHeight(); y++) {

                    pw.setArgb(x, y, image.getRGB(x, y));
                }
            }
        }

        return new ImageView(wr);
    }

    public List<String> getSelections() {

        return selections;
    }
}
