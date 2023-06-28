package com.pdfx.home.progress;

import com.pdfx.util.FileManagerUtil;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.kordamp.ikonli.javafx.FontIcon;

public class CompletedProgressView {

    private final VBox parentVbox;


    public CompletedProgressView(String message, String path) {

        this.parentVbox = ProgressViewInitializer.getVbox();
        initialize(message, path);
    }

    private void initialize(String message, String path) {

        ProgressViewInitializer.addRegion();
        VBox vBox = new VBox();
        vBox.setLayoutX(10.0);
        vBox.setLayoutY(73.0);
        vBox.getStyleClass().add("task-vbox");

        HBox hBox = new HBox();
        Label taskCompletedLabel = new Label(message);
        taskCompletedLabel.setMaxHeight(1.7976931348623157E308);
        taskCompletedLabel.setPrefWidth(225.0);
        taskCompletedLabel.setWrapText(true);
        HBox.setHgrow(taskCompletedLabel, Priority.ALWAYS);
        taskCompletedLabel.setFont(new Font("System Bold", 14.0));
        HBox.setMargin(taskCompletedLabel, new Insets(0, 20.0, 0, 20.0));

        Region region = new Region();
        region.setPrefHeight(60.0);
        region.setPrefWidth(20.0);

        Button openInFolderButton = new Button("Open in Folder");
        openInFolderButton.setMnemonicParsing(false);
        openInFolderButton.getStyleClass().addAll("button-icon", "small", "accent", "button-outlined", "button-circle");
        openInFolderButton.setTooltip(new Tooltip("Open in Parent Folder"));
        openInFolderButton.setCursor(Cursor.HAND);
        openInFolderButton.setGraphic(new FontIcon("fltfal-folder-open-16"));
        openInFolderButton.setOnAction(event -> FileManagerUtil.openInDefaultFileManager(path));
        HBox.setMargin(openInFolderButton, new Insets(15.0, 0, 0, 20.0));

        Button clearButton = new Button("Clear");
        clearButton.setMnemonicParsing(false);
        clearButton.getStyleClass().addAll("button-icon", "small", "button-outlined", "button-circle", "danger");
        clearButton.setTooltip(new Tooltip("Clear"));
        clearButton.setCursor(Cursor.HAND);
        clearButton.setGraphic(new FontIcon("fltfal-dismiss-16"));
        clearButton.setOnAction(event -> parentVbox.getChildren().remove(vBox));
        HBox.setMargin(clearButton, new Insets(15.0, 20.0, 0, 10.0));
        Region regionPadding = new Region();
        regionPadding.setPrefHeight(20.0);

        hBox.getChildren().addAll(taskCompletedLabel, region, openInFolderButton, clearButton);

        vBox.getChildren().addAll(hBox, regionPadding);
        parentVbox.getChildren().add(1, vBox);
    }
}
