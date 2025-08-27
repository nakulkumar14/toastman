package com.toast.demo.components;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ResponseSection extends VBox {

    private final TextArea responseArea = new TextArea();
    private Label statusCodeLabel = new Label();
    private Button copyButton = new Button("Copy");

    public ResponseSection() {
        setSpacing(10);
        setPadding(new Insets(10));

        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        responseArea.setPrefHeight(300);
        responseArea.setPromptText("Response will appear here...");

        copyButton.setTooltip(new Tooltip("Copy to clipboard"));
        copyButton.setOnAction(e -> handleCopy());

        HBox responseLabelBox = new HBox(5, new Label("Response:"), copyButton);
        statusCodeLabel.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(responseLabelBox, responseArea, statusCodeLabel);
    }

    public void setResponseBody(String body) {
        responseArea.setText(body);
    }

    public void setStatusCode(int code) {
        statusCodeLabel.setText("Status: " + code);
        String color = switch (code / 100) {
            case 2 -> "green";
            case 3 -> "blue";
            case 4 -> "orange";
            case 5 -> "red";
            default -> "black";
        };
        statusCodeLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
    }

    public void setError(String message) {
        responseArea.setText("Error: " + message);
        statusCodeLabel.setText("");
    }

    private void handleCopy() {
        String responseText = responseArea.getText();
        if (!responseText.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(responseText);
            Clipboard.getSystemClipboard().setContent(content);

            String originalText = copyButton.getText();
            copyButton.setText("Copied!");
            copyButton.setDisable(true);

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
                }

                Platform.runLater(() -> {
                    copyButton.setText(originalText);
                    copyButton.setDisable(false);
                });
            }).start();
        }
    }

    public TextArea getResponseArea() {
        return responseArea;
    }

    public Label getStatusCodeLabel() {
        return statusCodeLabel;
    }

    public void setStatusCodeLabel(Label statusCodeLabel) {
        this.statusCodeLabel = statusCodeLabel;
    }
}