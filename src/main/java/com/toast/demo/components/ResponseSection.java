package com.toast.demo.components;

import com.toast.demo.util.JsonHighlighter;
import com.toast.demo.util.StatusUtils;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.StyleClassedTextArea;

public class ResponseSection extends VBox {

    private final StyleClassedTextArea responseArea = new StyleClassedTextArea();
    private Label statusCodeLabel = new Label();
    private Button copyButton = new Button("Copy");

    public ResponseSection() {
        setSpacing(10);
        setPadding(new Insets(10));

        setupUI();
        setupCopyAction();

        copyButton.setTooltip(new Tooltip("Copy to clipboard"));
    }

    private void setupUI() {
        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        responseArea.setPrefHeight(300);
//        responseArea.setPromptText("Response will appear here..."); // can be removed

        statusCodeLabel.setStyle("-fx-font-weight: bold;");

        HBox responseHeader = new HBox(10, new Label("Response:"), copyButton);

        this.setSpacing(10);
        this.getChildren().addAll(responseHeader, responseArea, statusCodeLabel);
    }

    public void setResponseBody(String body) {
        JsonHighlighter.highlightJson(responseArea, body);
    }

    public void setError(String message) {
//        responseArea.setText("Error: " + message);
        statusCodeLabel.setText("");
    }

    public void setStatusCode(int statusCode) {
        String color = StatusUtils.getColorForStatusCode(statusCode);
        statusCodeLabel.setText("Status: " + statusCode);
        statusCodeLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    private void setupCopyAction() {
        copyButton.setOnAction(e -> {
            String text = responseArea.getText();
            if (!text.isEmpty()) {
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                Clipboard.getSystemClipboard().setContent(content);

                String original = copyButton.getText();
                copyButton.setText("Copied!");
                copyButton.setDisable(true);

                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(() -> {
                        copyButton.setText(original);
                        copyButton.setDisable(false);
                    });
                }).start();
            }
        });
    }

    public Label getStatusCodeLabel() {
        return statusCodeLabel;
    }

    public void setStatusCodeLabel(Label statusCodeLabel) {
        this.statusCodeLabel = statusCodeLabel;
    }
}