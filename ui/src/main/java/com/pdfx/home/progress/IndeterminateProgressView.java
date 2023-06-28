package com.pdfx.home.progress;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class IndeterminateProgressView {

    private final VBox parentVbox;
    private VBox vBox;
    private Label staticTaskLabel;
    private ProgressBar indeterminateProgressBar;

    public IndeterminateProgressView() {

        this.parentVbox = ProgressViewInitializer.getVbox();
        initialize();
    }

    private void initialize() {

        ProgressViewInitializer.addRegion();
        vBox = new VBox();
        vBox.getStyleClass().add("task-vbox");
        HBox hBox2 = new HBox();
        staticTaskLabel = new Label("3 out of 5 files splitted");
        staticTaskLabel.setPrefHeight(60.0);
        staticTaskLabel.setWrapText(true);
        HBox.setMargin(staticTaskLabel, new Insets(0, 20.0, 0, 20.0));
        hBox2.getChildren().add(staticTaskLabel);

        HBox hBox3 = new HBox();
        indeterminateProgressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
        indeterminateProgressBar.setPrefHeight(4.0);
        indeterminateProgressBar.setPrefWidth(305.0);
        HBox.setMargin(indeterminateProgressBar, new Insets(0, 20.0, 0, 20.0));
        Region regionPadding = new Region();
        regionPadding.setPrefHeight(20.0);
        hBox3.getChildren().add(indeterminateProgressBar);
        vBox.getChildren().addAll(hBox2, hBox3, regionPadding);
        parentVbox.getChildren().add(vBox);
    }

    public void finish() {

        parentVbox.getChildren().remove(vBox);
    }

    public StringProperty TaskDescriptionProperty() {

        return staticTaskLabel.textProperty();
    }

    public DoubleProperty ProgressProperty() {

        return indeterminateProgressBar.progressProperty();
    }
}
