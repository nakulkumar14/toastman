package com.toast.demo.components;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.StyledTextArea;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class CurlPane extends VBox {

//    private final TextArea curlArea = new TextArea();
    private final StyledTextArea styledTextArea = new StyleClassedTextArea();
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
//        curlArea.setWrapText(true);
//        curlArea.setEditable(false);
//        curlArea.setStyle("-fx-font-family: 'monospace';");

        styledTextArea.setWrapText(true);
        styledTextArea.setEditable(false);
        styledTextArea.setStyle("-fx-font-family: 'monospace';");

        //        VBox.setVgrow(curlArea, Priority.ALWAYS);

//        ScrollPane scrollPane = new ScrollPane(curlArea);
        ScrollPane scrollPane = new ScrollPane(styledTextArea);
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

        styledTextArea.textProperty().addListener((obs, oldText, newText) -> {
            applySyntaxHighlighting((String) newText);
        });
    }

    private static final Pattern CURL_PATTERN = Pattern.compile(
        "(-X\\s+(GET|POST|PUT|DELETE|PATCH)|--header\\s+('[^']+'|\"[^\"]+\")|--data-raw\\s+('[^']+'|\"[^\"]+\")|--url\\s+('[^']+'|\"[^\"]+\")|\\s*curl|https?://[a-zA-Z0-9./?#&%=+-]+\\s*)"
    );


    private void applySyntaxHighlighting(String text) {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        Matcher matcher = CURL_PATTERN.matcher(text);
        int lastKwEnd = 0;

        while (matcher.find()) {
            String styleClass = "plain-text";
            String matchedGroup = matcher.group();
            if (matchedGroup.contains("-X")) {
                styleClass = "method-keyword";
            } else if (matchedGroup.startsWith("--header") || matchedGroup.contains("url")) {
                styleClass = "header-keyword";
            } else if (matchedGroup.startsWith("--data-raw")) {
                styleClass = "data-keyword";
            } else if (matchedGroup.startsWith("curl")) {
                styleClass = "curl-keyword";
            } else if (matchedGroup.startsWith("http")) {
                styleClass = "url-text";
            }

            spansBuilder.add(Collections.singleton("plain-text"), matcher.start() - lastKwEnd);
            spansBuilder.add(Collections.singleton(styleClass), matcher.end() - matcher.start());
            lastKwEnd = matcher.end();
        }
        spansBuilder.add(Collections.singleton("plain-text"), text.length() - lastKwEnd);
        styledTextArea.setStyleSpans(0, spansBuilder.create());
    }

    private void copyCurlToClipboard() {
        String text = styledTextArea.getText().trim();
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
        styledTextArea.replaceText(0, styledTextArea.getLength(), command);
    }

    public void setOnClose(Runnable onClose) {
        this.onCloseCallback = onClose;
    }
}
