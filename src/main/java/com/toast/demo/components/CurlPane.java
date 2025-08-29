package com.toast.demo.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CurlPane extends VBox {

    private final TextArea curlArea = new TextArea();
    private final Button copyButton = new Button("Copy");
    private final Button closeButton = new Button("Close");

    private Runnable onCloseCallback;

    public CurlPane() {
        setupUI();
        setupActions();
    }

    private void setupUI() {
        setSpacing(10);
        setPadding(new Insets(10));
        setMinWidth(300);
        setStyle("-fx-background-color: #f4f4f4;");

        // cURL text area
        curlArea.setWrapText(true);
        curlArea.setEditable(false);
        curlArea.setStyle("-fx-font-family: 'monospace';");
        //        VBox.setVgrow(curlArea, Priority.ALWAYS);

        ScrollPane scrollPane = new ScrollPane(curlArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Top bar with controls
        HBox topBar = new HBox(10, new Label("cURL Preview:"), copyButton, closeButton);

        getChildren().addAll(topBar, scrollPane);
    }

    private void setupActions() {
        copyButton.setOnAction(e -> copyCurlToClipboard());
        closeButton.setOnAction(e -> {
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        });
    }

    private void copyCurlToClipboard() {
        String text = curlArea.getText().trim();
        if (text.isEmpty()) {
            return;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);

        // Temporary UI feedback
        copyButton.setText("Copied!");
        copyButton.setDisable(true);

        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
            javafx.application.Platform.runLater(() -> {
                copyButton.setText("Copy");
                copyButton.setDisable(false);
            });
        }).start();
    }

    public void setCurlCommand(String command) {
        curlArea.setText(command);
    }

    public void setOnClose(Runnable onClose) {
        this.onCloseCallback = onClose;
    }
}
