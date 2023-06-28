package com.pdfx.pdf.compress;

import com.pdfx.compress.Zipper;
import com.pdfx.fileshandler.FileType;
import com.pdfx.fileshandler.FilesManagement;
import com.pdfx.home.progress.CompletedProgressView;
import com.pdfx.home.progress.DeterminateProgressView;
import com.pdfx.home.progress.IndeterminateProgressView;
import com.pdfx.logging.StackTraceUtil;
import com.pdfx.thread.SingletonExecutorService;
import com.pdfx.util.CustomAlerts;
import com.pdfx.util.CustomCheckBoxListCell;
import com.pdfx.util.FilePicker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;

public class CompressController {

    @FXML
    private ListView<String> listView;

    @FXML
    private RadioButton speedRadio;

    @FXML
    private RadioButton defaultRadio;

    @FXML
    private RadioButton sizeRadio;

    @FXML
    private CheckBox separatelyCheckBox;

    private List<File> fileList;
    private ObservableList<String> fileNamesList;

    public CompressController() {

        this.fileList = FilesManagement.getFileListByType(FileType.PDF).orElseThrow();
    }

    @FXML
    private void initialize() {

        fileNamesList = FXCollections.observableList(fileList.stream()
                .map(File::getName)
                .collect(Collectors.toList())
        );

        new CustomCheckBoxListCell<>(listView, fileNamesList).displayItems();
    }

    @FXML
    private void handleCompressButton() {

        Set<String> fileNamesSet = new HashSet<>(fileNamesList);
        fileList = fileList.stream()
                .filter(file -> fileNamesSet.contains(file.getName()))
                .collect(Collectors.toList());

        zip();
    }

    private int getCompressLevel() {

        if (defaultRadio.isSelected())
            return -1;
        else if (speedRadio.isSelected())
            return 1;
        else if (sizeRadio.isSelected())
            return 9;
        else
            return 0;
    }

    private void zip() {

        if (separatelyCheckBox.isSelected())
            zipIndividualFiles();

        else {

            String filename;
            try {

                filename = FilePicker.askToSaveFile(listView, "Zip Files", "zip").getAbsolutePath();
                if (filename.isBlank())
                    return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            int total = fileList.size();
            IndeterminateProgressView indeterminateProgressView = new IndeterminateProgressView();
            var progressBarProperty = indeterminateProgressView.ProgressProperty();
            indeterminateProgressView.TaskDescriptionProperty().set("out of " + total + " files compressed");

            var executorService = SingletonExecutorService.getInstance();
            Task<Void> zipTask = new Task<>() {

                @Override
                protected Void call() throws IOException {

                    Zipper.zip(fileList, filename, getCompressLevel());
                    return null;
                }
            };

            executorService.submit(zipTask);
            progressBarProperty.bind(zipTask.progressProperty());

            zipTask.setOnSucceeded(workerStateEvent -> {

                try {
                    indeterminateProgressView.finish();
                    new CompletedProgressView(String.valueOf(total).concat(" files have been zipped"), fileList.get(0).getParent());
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Access Denied", "Invalid file/directory path", "File may have been moved or deleted",
                            Alert.AlertType.ERROR);
                } finally {
                    FilesManagement.removeFileListByType(FileType.PDF);
                    listView.getScene().getWindow().hide();
                }
            });
        }
    }

    private void zipIndividualFiles() {

        int total = fileList.size();
        DeterminateProgressView determinateProgressView = new DeterminateProgressView();
        var progressBarProperty = determinateProgressView.ProgressProperty();
        determinateProgressView.TaskDescriptionProperty().set("out of " + total + " files compressed");

        var executorService = SingletonExecutorService.getInstance();
        List<Callable<Void>> callableList = new ArrayList<>(total);

        Task<Void> zipTask = new Task<>() {

            @Override
            protected Void call() {

                for (int i = 0; i < total; i++) {

                    int finalI = i;
                    Callable<Void> callable = () -> {

                        Zipper.zip(fileList.get(finalI), fileList.get(finalI).getName().concat("-encrypted"), getCompressLevel());
                        updateProgress(finalI + 1, total);
                        updateMessage(String.valueOf(finalI + 1));
                        return null;
                    };
                    callableList.add(callable);
                }
                try {

                    executorService.invokeAll(callableList);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    CustomAlerts.alert("Compression Error", "Files may not have been compressed",
                            StackTraceUtil.getStackTrace(e));
                }
                return null;
            }
        };

        executorService.submit(zipTask);
        progressBarProperty.bind(zipTask.progressProperty());
        determinateProgressView.DynamicTextProperty().bind(zipTask.messageProperty());

        zipTask.setOnSucceeded(workerStateEvent -> {

            try {

                determinateProgressView.finish();
                new CompletedProgressView(String.valueOf(total).concat(" files have been zipped"), fileList.get(0).getParent());
            } catch (NullPointerException e) {
                e.printStackTrace();
                CustomAlerts.alert("Access Denied", "Invalid file/directory path", "File may have been moved or deleted",
                        Alert.AlertType.ERROR);
            } finally {
                FilesManagement.removeFileListByType(FileType.PDF);
                listView.getScene().getWindow().hide();
            }
        });
    }
}
