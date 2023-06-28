package com.pdfx.home.progress;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class ProgressViewInitializer {

    private static VBox parentVbox;

    public ProgressViewInitializer(ScrollPane scrollPane) {

        if (parentVbox == null) {

            parentVbox = new VBox();
            scrollPane.setContent(parentVbox);
        }

        initialize();
    }

    public static VBox getVbox() {

        return parentVbox;
    }

    private HBox getTaskTitle() {

        addRegion();
        HBox titleHbox = new HBox();
        Label titleLabel = new Label("Tasks in Progress");
        titleLabel.setMaxHeight(1.7976931348623157E308);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);
        titleLabel.setFont(new Font("Segoe UI Bold", 18.0));
        HBox.setMargin(titleLabel, new Insets(0, 20.0, 0, 20.0));
        titleHbox.getChildren().add(titleLabel);
        return titleHbox;
    }

    protected static void addRegion() {

        Region upperRegion = new Region();
        upperRegion.setPrefHeight(20.0);
        parentVbox.getChildren().add(upperRegion);
    }

    private void initialize() {

        parentVbox.getStylesheets().add(getClass().getResource("/fxml/css/nord-dark.css").toExternalForm());
        parentVbox.getStylesheets().add(getClass().getResource("/fxml/css/ProgressVboxStyle.css").toExternalForm());
        parentVbox.setMaxHeight(Double.NEGATIVE_INFINITY);
        parentVbox.setMaxWidth(Double.NEGATIVE_INFINITY);
        parentVbox.setMinHeight(Double.NEGATIVE_INFINITY);
        parentVbox.setMinWidth(Double.NEGATIVE_INFINITY);
        parentVbox.setPrefWidth(338.0);

        HBox titleHbox = getTaskTitle();
        parentVbox.getChildren().add(titleHbox);
    }
}
