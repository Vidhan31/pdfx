package com.pdfx.home.progress;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DeterminateProgressView {

    private final VBox parentVbox;
    private VBox vBox;
    private Label dynamicTaskLabel;
    private Label dynamicTaskDescriptionLabel;
    private ProgressBar determinateProgressBar;

    public DeterminateProgressView() {

        this.parentVbox = ProgressViewInitializer.getVbox();
        initialize();
    }

    private void initialize() {

        vBox = new VBox();
        vBox.setLayoutX(10.0);
        vBox.setLayoutY(73.0);
        vBox.getStyleClass().add("task-vbox");

        HBox hBox1 = new HBox();
        dynamicTaskLabel = new Label();
        dynamicTaskLabel.setWrapText(true);
        dynamicTaskLabel.setMaxHeight(1.7976931348623157E308);
        HBox.setHgrow(dynamicTaskLabel, Priority.ALWAYS);
        HBox.setMargin(dynamicTaskLabel, new Insets(0, 20.0, 0, 20.0));
        dynamicTaskDescriptionLabel = new Label();
        dynamicTaskDescriptionLabel.setLayoutX(30.0);
        dynamicTaskDescriptionLabel.setLayoutY(10.0);
        dynamicTaskDescriptionLabel.setPrefHeight(60.0);
        dynamicTaskDescriptionLabel.setWrapText(true);
        HBox.setMargin(dynamicTaskDescriptionLabel, new Insets(0, 20.0, 0, 7.0));
        hBox1.getChildren().addAll(dynamicTaskLabel, dynamicTaskDescriptionLabel);

        HBox hBox2 = new HBox();
        determinateProgressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        determinateProgressBar.setPrefHeight(4.0);
        determinateProgressBar.setPrefWidth(305.0);
        HBox.setMargin(determinateProgressBar, new Insets(0, 20.0, 0, 20.0));
        hBox2.getChildren().add(determinateProgressBar);
        Region regionPadding = new Region();
        regionPadding.setPrefHeight(20.0);

        vBox.getChildren().addAll(hBox1, hBox2, regionPadding);
        parentVbox.getChildren().add(vBox);
    }

    public void finish() {

        parentVbox.getChildren().remove(vBox);
    }

    public StringProperty DynamicTextProperty() {

        return dynamicTaskLabel.textProperty();
    }

    public StringProperty TaskDescriptionProperty() {

        return dynamicTaskDescriptionLabel.textProperty();
    }

    public DoubleProperty ProgressProperty() {

        return determinateProgressBar.progressProperty();
    }
}
