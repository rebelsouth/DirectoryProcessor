package ru.rebelsouth.demo1.Analyzer;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HelloApplication extends Application {
    private ProgressBar progressBar;
    private TextArea depthInput;
    private Label statusLabel;
    private Path selectedPath;
    private int depth;
    private Set<Long> dbIds = new HashSet<>();

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("УБИЙЦА ПАПОК");

        depthInput = new TextArea();
        depthInput.setPromptText("Введите глубину ");
        depthInput.setPrefHeight(40);

        Button loadFromFileButton = new Button("Загрузить ID из файла");
        loadFromFileButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл с ID");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
            File selectedFile = fileChooser.showOpenDialog(stage);
            if (selectedFile != null) {
                dbIds.clear();
                try {
                    List<String> lines = Files.readAllLines(selectedFile.toPath());
                    for (String line : lines) {
                        String[] ids = line.split("[,\\s]+"); // делим по пробелу или запятой
                        for (String id : ids) {
                            if (!id.isBlank()) {
                                dbIds.add(Long.parseLong(id.trim()));
                            }
                        }
                    }
                    statusLabel.setText("Загружено ID из файла: " + dbIds.size());
                } catch (IOException | NumberFormatException e) {
                    statusLabel.setText("Ошибка чтения файла ID: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                statusLabel.setText("Файл не выбран.");
            }
        });

        Button setDepthButton = new Button("Установить глубину");
        setDepthButton.setOnAction(event -> {
            try {
                depth = Integer.parseInt(depthInput.getText());
                statusLabel.setText("Глубина установлена: " + depth);
            } catch (NumberFormatException e) {
                statusLabel.setText("Некорректная глубина, введи другую");
            }
        });


        Button chooseDirButton = new Button("Выбери директорию");
        chooseDirButton.setOnAction(event -> {
            File selectedDir = new DirectoryChooser().showDialog(stage);
            if (selectedDir != null) {
                selectedPath = selectedDir.toPath();
                statusLabel.setText("Выбрана: " + selectedPath);
            }
        });

        Button buildTreeButton = new Button("Построение префиксного дерева");
        PrefixTree prefixTree = PrefixTree.getInstance();
        buildTreeButton.setOnAction(event -> {
            if (dbIds.isEmpty()) {
                statusLabel.setText("Сначала надо отпарсить как следует дб");
                return;
            }

            prefixTree.clear(); // очистка дерева перед построением
            for (Long id : dbIds) {
                String normalized = String.format("%0" + depth + "d", id);
                prefixTree.insert(normalized);
            }
            statusLabel.setText("Построено префиксное дерево с " + dbIds.size() + " вхождениями");
        });

        Button processButton = new Button("Очистка директорий");
        processButton.setOnAction(event -> {
            if (selectedPath == null) {
                statusLabel.setText("Сначала нужно выбрать директорию");
                return;
            }
            if (depth == 0) {
                statusLabel.setText("Сначала нужно выбрать глубину!");
                return;
            }
            if (prefixTree.isEmpty()) {
                statusLabel.setText("Сначала нужно построить префиксное дерево!");
                return;
            }

            progressBar.setProgress(0);
            progressBar.setVisible(true);
            statusLabel.setText("Выполнение...");

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    DirectoryProcessor processor = new DirectoryProcessor(selectedPath, depth);

                    for (int i = 1; i <= 10; i++) {
                        Thread.sleep(100);
                        updateProgress(i, 10);
                    }

                    processor.cleanFileSystem(prefixTree);

                    return null;
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    statusLabel.setText("Программа выполнена успешно!");
                    progressBar.setVisible(false);
                }

                @Override
                protected void failed() {
                    super.failed();
                    statusLabel.setText("Ошибка: " + getException().getMessage());
                    getException().printStackTrace();
                    progressBar.setVisible(false);
                }
            };

            progressBar.progressProperty().bind(task.progressProperty());

            new Thread(task).start();
        });
//        processButton.setOnAction(event -> {
//            if (selectedPath == null) {
//                statusLabel.setText("Сначала выбери директорию");
//                return;
//            }
//            if (depth == 0) {
//                statusLabel.setText("Сначала напиши глубину");
//                return;
//            }
//            if (prefixTree.isEmpty()) {
//                statusLabel.setText("Сначала построй префиксное дерево");
//                return;
//            }
//
//            try {
//                DirectoryProcessor processor = new DirectoryProcessor(selectedPath, depth);
//                processor.cleanFileSystem(prefixTree);
//                statusLabel.setText("УСПЕШНО!!!!!");
//            } catch (Exception e) {
//                statusLabel.setText("Ошибка: " + e.getMessage());
//                e.printStackTrace();
//            }
//        });

//        Button showTreeButton = new Button("Показать структуру дерева (хуево)");
//        showTreeButton.setOnAction(event -> {
//            if (prefixTree.isEmpty()) {
//                statusLabel.setText("Дерево пустое");
//            } else {
//                statusLabel.setText("Структура дерева:\n" + prefixTree.toString());
//            }
//        });

        statusLabel = new Label("Статус: Ожидание ввода");
        statusLabel.setWrapText(true);

        VBox root = new VBox(10,
                new Label("обработка папок"),
                new Separator(),
                depthInput,
                setDepthButton,
                new Separator(),
                loadFromFileButton,
                new Separator(),
                chooseDirButton,
                buildTreeButton,
                processButton,
//                showTreeButton,
                new Separator(),
                statusLabel
        );
        root.setStyle("-fx-padding: 20; -fx-spacing: 10;");
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setVisible(true);

        stage.setScene(new Scene(root, 500, 600));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}


