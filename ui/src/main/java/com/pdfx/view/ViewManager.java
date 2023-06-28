package com.pdfx.view;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * The `ViewManager` class provides a way to manage different views. It is implemented as a singleton pattern so that
 * only one instance of the `ViewManager` can exist at a time.
 */
public class ViewManager {

    private static volatile ViewManager instance;
    private static final Map<View, Parent> sceneRoots;
    private final Stage stage;

    static {
        sceneRoots = new HashMap<>();
    }

    /**
     * Private constructor to prevent direct instantiation of the `ViewManager` class.
     *
     * @param stage the `Stage` object on which the scenes will be displayed
     */
    private ViewManager(Stage stage) {
        this.stage = stage;
    }

    /**
     * This method is thread-safe. It uses the double-checked locking pattern to ensure that only one instance of the
     * `ViewManager` class is created. This method is synchronized so that only one thread can access it at a time.
     *
     * @param stage the `Stage` object on which the scenes will be displayed
     *
     * @return the single instance of the `ViewManager` class
     */
    public static ViewManager getInstance(Stage stage) throws IOException {

        if (instance == null) {

            synchronized (ViewManager.class) {

                if ((instance == null) && (stage != null))
                    instance = new ViewManager(stage);
            }
        }
        return instance;
    }

    /**
     * Activates a scene with the given name. The scene is then displayed on the `Stage` object associated with the
     * `ViewManager`.
     *
     * @param view      scene to be shown
     * @param title     the title of the scene
     * @param resizable whether the scene should be resizable or not
     * @param maximized whether the scene should be maximized or not
     */
    public void showView(View view, String title, boolean resizable, boolean maximized) {

        Parent root = sceneRoots.get(view);
        Scene scene = stage.getScene() == null ? new Scene(root) : stage.getScene();
        scene.setRoot(root);
        scene.setUserAgentStylesheet(getClass().getResource("/fxml/css/nord-dark.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle(title);
        stage.setResizable(resizable);
        stage.setMaximized(maximized);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Adds a scene to the `ViewManager` with a given name if it doesn't exist. If it exists, previous one is replaced
     * with a new scene
     *
     * @param view the name of the scene
     * @param fxml the file path of the FXML file defining the scene
     */
    public void loadNewView(String fxml, View view) throws IOException {

        if (!sceneRoots.containsKey(view))
            storeViewState(fxml, view);

        else {

            sceneRoots.remove(view);
            storeViewState(fxml, view);
        }
    }

    /**
     * Adds a scene with its current state to the `ViewManager` with a given name.
     *
     * @param view the name of the scene
     * @param fxml the file path of the FXML file defining the scene
     */
    public void storeViewState(String fxml, View view) throws IOException {

        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        sceneRoots.put(view, root);
    }

    /**
     * Removes the stored scene
     *
     * @param view of the scene to be removed
     */
    public static void removeView(View view) {

        if (sceneRoots.containsKey(view)) {

            sceneRoots.remove(view, null);
            sceneRoots.remove(view);
        }
    }

    /**
     * @param fxml file to load on Dialog
     *
     * @return CustomDialog object which can be used to perform customized operations
     *
     * @throws IOException if there is error loading fxml file
     */
    public CustomDialog loadDialog(String fxml) throws IOException {

        return new CustomDialog(fxml, stage);
    }
}
