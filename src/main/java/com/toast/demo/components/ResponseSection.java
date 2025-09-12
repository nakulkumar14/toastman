package com.toast.demo.components;

import com.toast.demo.util.HtmlHighlighter;
import com.toast.demo.util.JsonFormatter;
import com.toast.demo.util.JsonHighlighter;
import com.toast.demo.util.StatusUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import javafx.stage.FileChooser;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseSection extends VBox {

    private static final Logger log = LoggerFactory.getLogger(ResponseSection.class);

    private static final String JSON_TYPE = "application/json";
    private static final String HTML_TYPE = "text/html";

    private final StyleClassedTextArea responseArea = new StyleClassedTextArea();
    private final Label statusCodeLabel = new Label();
    private final Button copyButton = new Button("Copy");

    private final VBox headersBox = new VBox(5);
    private final TabPane responseTabs = new TabPane();

    private final Button saveResponseButton = new Button("Save Response");

    public ResponseSection() {
        setSpacing(10);
        setPadding(new Insets(10));

        setupUI();
        setupCopyAction();
        setupSaveResponseAction();

        copyButton.setTooltip(new Tooltip("Copy to clipboard"));
    }

    private void setupUI() {
        responseArea.setEditable(false);
        responseArea.setWrapText(true);
        responseArea.setPrefHeight(300);
        responseArea.setStyle("-fx-font-family: monospace;");

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

        copyButton.setTooltip(new Tooltip("Copy response body to clipboard"));

        saveResponseButton.setTooltip(new Tooltip("Save response to file"));
        // Top bar
        HBox responseTopBar = new HBox(10, new Label("Response:"), copyButton, saveResponseButton);
        statusCodeLabel.setStyle("-fx-font-weight: bold;");

        getChildren().addAll(responseTopBar, responseTabs, statusCodeLabel);
    }

    public void setResponseBody(String body, String contentType) {
        if (contentType == null) {
            contentType = "";
        }
        responseArea.clear();
        if (contentType.contains(JSON_TYPE)) {
            JsonHighlighter.highlightJson(responseArea, JsonFormatter.prettyPrint(body));
        } else if (contentType.contains(HTML_TYPE)) {
            HtmlHighlighter.highlightHtml(responseArea, body);
        } else {
            responseArea.appendText(body);
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

                giveCopyFeedback();
            }
        });
    }

    private void setupSaveResponseAction() {
        saveResponseButton.setOnAction(e -> {
            // Create a new FileChooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Response");

            // Set the initial directory (optional)
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            // Set file extension filters to suggest file types
            FileChooser.ExtensionFilter jsonFilter = new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json");
            FileChooser.ExtensionFilter textFilter = new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().addAll(jsonFilter, textFilter);

            // Show the save dialog and get the selected file
            File file = fileChooser.showSaveDialog(saveResponseButton.getScene().getWindow());

            if (file != null) {
                // Get the content to be saved (assuming you have a method to retrieve it)
                String content = responseArea.getText(); // You need to implement this method

                // Save the content to the selected file
                saveFile(content, file);
            }
        });
    }

    private void saveFile(String content, File file) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content);
            log.info("File saved successfully to: " + file.getAbsolutePath());
        } catch (IOException ex) {
            log.error("Error saving file: ", ex);
        }
    }

    private void giveCopyFeedback() {
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