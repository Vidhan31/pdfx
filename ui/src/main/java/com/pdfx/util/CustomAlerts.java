package com.pdfx.util;

import java.util.Objects;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import static javafx.scene.control.Alert.AlertType;
import static javafx.scene.control.Alert.AlertType.ERROR;

/**
 * The class provides methods to show different types of alert boxes.
 */
public class CustomAlerts {

    /**
     * This method is used to show an alert box with the given title and alert type.
     *
     * @param title   The title of the alert box.
     * @param ordinal The type of alert box to be displayed.
     */
    public static void alert(String title, int ordinal) {

        Platform.runLater(() -> {

            var alert = new Alert(ERROR);
            DialogPane dialogPane = Objects.requireNonNull(alert).getDialogPane();
            dialogPane.getStylesheets().add(CustomAlerts.class.getResource("/fxml/css/nord-dark.css").toExternalForm());
            alert.setTitle(title);

            switch (ordinal) {

                case 0 -> {

                    alert.setHeaderText("Error connecting to the internet");
                    alert.setContentText("Please retry after connecting to the Internet");
                    alert.showAndWait();
                }

                case 1 -> {

                    alert.setHeaderText("Email Already Exists");
                    alert.setContentText("Log-in instead or try with different credentials");
                    alert.showAndWait();
                }

                case 2 -> {

                    alert.setHeaderText("Username Already Exists");
                    alert.setContentText("Log-in instead or try with different credentials");
                    alert.showAndWait();
                }

                case 3 -> {

                    alert.setHeaderText("Invalid Email");
                    alert.setContentText("Proper email format example: host@domain.com");
                    alert.showAndWait();
                }

                case 4 -> {

                    alert.setHeaderText("Invalid Password");
                    alert.setContentText("Password must be between 8 and 20 characters long and must contain at least" +
                            " one" +
                            "digit, one uppercase letter, one lowercase letter and one special character");
                    alert.showAndWait();
                }

                case 5 -> {

                    alert.setHeaderText("Invalid Username");
                    alert.setContentText("Username must be between 3 and 15 characters long and can contain only " +
                            "letters," +
                            "numbers, underscores and hyphens.");
                    alert.showAndWait();
                }

                case 6 -> {

                    alert.setHeaderText("Invalid details");
                    alert.setContentText("Please re-check your credentials");
                    alert.showAndWait();
                }

                case 7 -> {

                    alert.setHeaderText("Success!");
                    alert.setContentText("Please log-in to continue");
                    alert.showAndWait();
                }

                default -> throw new IllegalStateException("Unexpected value: " + ordinal);
            }
        });
    }

    public static void build(String stackTrace, Alert alert, Label label) {

        TextArea textArea = new TextArea(stackTrace);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setStyle("-fx-text-fill: black;");
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * This overloaded method is used to show an alert box with the stacktrace of an exception.
     *
     * @param title      The title of the alert box.
     * @param stackTrace The stack trace of the exception to be displayed in the alert box.
     */
    public static void alert(String title, String stackTrace) {

        Platform.runLater(() -> {
            var alert = new Alert(ERROR);
            alert.setTitle(title);
            alert.setHeaderText("Internal error");
            DialogPane dialogPane = Objects.requireNonNull(alert).getDialogPane();
            dialogPane.getStylesheets().add(CustomAlerts.class.getResource("/fxml/css/TextAreaStyle.css").toExternalForm());
            dialogPane.getStylesheets().add(CustomAlerts.class.getResource("/fxml/css/nord-dark.css").toExternalForm());

            Label label = new Label("The exception stacktrace was: ");

            build(stackTrace, alert, label);
        });
    }

    /**
     * This overloaded method is used to show an alert box with the stacktrace of an exception.
     *
     * @param title      The title of the alert box.
     * @param message    The message to be displayed.
     * @param stackTrace The stack trace of the exception to be displayed in the alert box.
     */
    public static void alert(String title, String message, String stackTrace) {

        Platform.runLater(() -> {

            var alert = new Alert(ERROR);
            alert.setTitle(title);
            alert.setHeaderText(message);
            DialogPane dialogPane = Objects.requireNonNull(alert).getDialogPane();
            dialogPane.getStylesheets().add(CustomAlerts.class.getResource("/fxml/css/TextAreaStyle.css").toExternalForm());
            dialogPane.getStylesheets().add(CustomAlerts.class.getResource("/fxml/css/nord-dark.css").toExternalForm());

            Label label = new Label("The exception stacktrace is below");

            build(stackTrace, alert, label);
        });
    }

    /**
     * This is overloaded method for conversion-related exceptions.
     *
     * @param title     Title of the window/stage
     * @param head      The main heading of the alert box.
     * @param message   Message to be displayed
     * @param alertType Type of alert to be shown
     */
    public static void alert(String title, String head, String message, AlertType alertType) {

        Platform.runLater(() -> {

            var alert = new Alert(alertType);
            alert.setTitle(title);
            alert.setHeaderText(head);
            DialogPane dialogPane = Objects.requireNonNull(alert).getDialogPane();
            dialogPane.setExpanded(false);
            dialogPane.getStylesheets().add(CustomAlerts.class.getResource("/fxml/css/nord-dark.css").toExternalForm());
            Label label = new Label(message);
            label.setVisible(true);

            alert.showAndWait();
        });
    }
}
