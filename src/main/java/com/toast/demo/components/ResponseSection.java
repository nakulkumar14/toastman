package com.toast.demo.components;

import com.toast.demo.util.HtmlHighlighter;
import com.toast.demo.util.JsonFormatter;
import com.toast.demo.util.JsonHighlighter;
import com.toast.demo.util.StatusUtils;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

    private final VBox headersBox = new VBox(5);
    private final TabPane responseTabs = new TabPane();

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

        ScrollPane bodyScroll = new ScrollPane(responseArea);
        bodyScroll.setFitToWidth(true);

        Tab bodyTab = new Tab("Body", bodyScroll);
        bodyTab.setClosable(false);

        // Headers tab
        ScrollPane headersScroll = new ScrollPane(headersBox);
        headersScroll.setFitToWidth(true);

        Tab headersTab = new Tab("Headers", headersScroll);
        headersTab.setClosable(false);

        responseTabs.getTabs().addAll(bodyTab, headersTab);
        responseTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Top bar
        HBox responseHeader = new HBox(10, new Label("Response:"), copyButton);
        statusCodeLabel.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(responseHeader, responseTabs, statusCodeLabel);

        this.setSpacing(10);
    }

    public void setResponseBody(String body, String contentType) {
        if (contentType.contains("application/json")) {
            JsonHighlighter.highlightJson(responseArea, JsonFormatter.prettyPrint(body));
        } else if (contentType.contains("text/html")) {
            HtmlHighlighter.highlightHtml(responseArea, body);
        } else {
            responseArea.clear();
            responseArea.appendText(body); // plain text fallback
        }
    }

    public void setResponseHeaders(Map<String, String> headers) {
        headersBox.getChildren().clear();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            Label key = new Label(entry.getKey() + ":");
            key.setStyle("-fx-font-weight: bold;");
            Label value = new Label(entry.getValue());

            HBox row = new HBox(10, key, value);
            row.setPadding(new Insets(2));
            headersBox.getChildren().add(row);
        }
    }

    public void setError(String message) {
//        responseArea.setText("Error: " + message);
//        statusCodeLabel.setText("");

        responseArea.replaceText("Error: " + message);
        headersBox.getChildren().clear();
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