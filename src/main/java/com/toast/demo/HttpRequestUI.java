package com.toast.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HttpRequestUI extends Application {

    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        setupPrimaryStage(primaryStage);
    }

    private void setupPrimaryStage(Stage stage) {
        RequestTabPane tabPane = new RequestTabPane();
        Scene scene = new Scene(tabPane, WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Multi‑Tab HTTP Client");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
