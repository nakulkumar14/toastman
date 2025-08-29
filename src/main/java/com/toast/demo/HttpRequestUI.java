package com.toast.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HttpRequestUI extends Application {

    @Override
    public void start(Stage primaryStage) {
        RequestTabPane tabPane = new RequestTabPane();
        Scene scene = new Scene(tabPane, 1200, 800);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Multi‑Tab HTTP Client");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
