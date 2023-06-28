package com.pdfx.util;

import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CheckBoxListCell;

public class CustomCheckBoxListCell<T> {

    private final ObservableList<T> items;
    private final ListView<T> listView;

    public CustomCheckBoxListCell(ListView<T> listView, ObservableList<T> items) {

        this.listView = listView;
        this.items = items;
    }

    public void displayItems() {

        listView.setCellFactory(CheckBoxListCell.forListView(item -> {

            // Create a CheckBox for each item in the list
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty().set(true);

            // Add a listener to the CheckBox's selected property
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {

                // Update the items list based on checkbox selection
                if (newValue)
                    items.add(item);
                else
                    items.remove(item);
            });

            // Return the selected property of the CheckBox
            return checkBox.selectedProperty();
        }));

        // Set the items to the ListView
        listView.getItems().setAll(items);
    }
}
