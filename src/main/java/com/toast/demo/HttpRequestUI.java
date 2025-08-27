package com.toast.demo;

import com.toast.demo.service.HttpRequestService;
import com.toast.demo.util.JsonFormatter;
import com.toast.demo.util.ToastConst;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HttpRequestUI extends Application {

    private final HttpRequestService httpRequestService = new HttpRequestService();

    private final TextField urlField = createUrlField();
    private final ComboBox<String> methodComboBox = createMethodComboBox();
    private final VBox headersBox = new VBox(5);
    private final VBox paramsBox = new VBox(5);
    private final TextArea bodyArea = createBodyArea();
    private final TextArea responseArea = createResponseArea();
    private final Label statusCodeLabel = new Label();
    private final Button copyButton = new Button("Copy");

    private boolean isSyncingParams = false;

    @Override
    public void start(Stage primaryStage) {

        HBox urlInputBar = createRequestInputBar();
        TabPane requestTabs = createRequestTabs();
        VBox responseSection = createResponseSection();

        VBox layout = new VBox(15,
            urlInputBar,
            requestTabs,
            responseSection
        );
        layout.setPadding(new Insets(20));

        urlField.textProperty().addListener((obs, oldVal, newVal) -> updateParamsFromUrl(newVal));

        primaryStage.setScene(new Scene(layout, 1000, 750));
        primaryStage.setTitle("HTTP Request Sender");
        primaryStage.show();
    }
    // ────────────────────────────── UI SECTIONS ──────────────────────────────

    private HBox createRequestInputBar() {
        Button sendButton = new Button("Send");
        sendButton.setMinWidth(100);
        urlField.setPrefWidth(700);

        sendButton.setOnAction(e -> executeRequest());

        HBox urlBox = new HBox(10, methodComboBox, urlField, sendButton);
        urlBox.setAlignment(Pos.CENTER_LEFT);
        return urlBox;
    }

    private TabPane createRequestTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab paramsTab = new Tab("Params", createParamEditor(paramsBox));
        Tab headersTab = new Tab("Headers", createHeaderEditor(headersBox));
        Tab bodyTab = new Tab("Body", new VBox(bodyArea));

        tabPane.getTabs().addAll(paramsTab, headersTab, bodyTab);
        return tabPane;
    }

    private VBox createResponseSection() {
        copyButton.setOnAction(e -> handleCopyAction());

        HBox responseLabelBox = new HBox(5, new Label("Response:"), copyButton);
        statusCodeLabel.setStyle("-fx-font-weight: bold;");

        return new VBox(10, responseLabelBox, responseArea, statusCodeLabel);
    }

//    private VBox createKeyValueEditor(VBox container, String keyPrompt, String valuePrompt) {
//        VBox editor = new VBox(5);
//        Button addButton = new Button("Add " + keyPrompt);
//        addButton.setOnAction(e -> {
//            TextField keyField = new TextField();
//            keyField.setPromptText(keyPrompt);
//
//            TextField valueField = new TextField();
//            valueField.setPromptText(valuePrompt);
//
//            Button removeButton = new Button("X");
//            HBox row = new HBox(10, keyField, valueField, removeButton);
//            row.setPadding(new Insets(5));
//            removeButton.setOnAction(ev -> container.getChildren().remove(row));
//            container.getChildren().add(row);
//        });
//
//        ScrollPane scrollPane = new ScrollPane(container);
//        scrollPane.setFitToWidth(true);
//        scrollPane.setPrefHeight(150);
//
//        return new VBox(5, addButton, scrollPane);
//    }

    private VBox createHeaderEditor(VBox container) {
        VBox editor = new VBox(5);
        Button addButton = new Button("Add Header");
        addButton.setOnAction(e -> {
            ComboBox<String> keyField = new ComboBox<>();
            keyField.setEditable(true);
            keyField.setPromptText("Header Name");
            keyField.getItems().addAll(ToastConst.COMMON_HEADERS);  // your predefined list

            TextField valueField = new TextField();
            valueField.setPromptText("Header Value");

            Button removeButton = new Button("X");
            HBox row = new HBox(10, keyField, valueField, removeButton);
            row.setPadding(new Insets(5));
            removeButton.setOnAction(ev -> container.getChildren().remove(row));

            container.getChildren().add(row);
        });

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);

        return new VBox(5, addButton, scrollPane);
    }

    // Params with sync
    private VBox createParamEditor(VBox container) {
        VBox editor = new VBox(5);
        Button addButton = new Button("Add Param");
        addButton.setOnAction(e -> addParamRow("", ""));
        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        return new VBox(5, addButton, scrollPane);
    }

    private void addParamRow(String key, String value) {
        TextField keyField = new TextField(key);
        TextField valueField = new TextField(value);
        keyField.setPromptText("Key");
        valueField.setPromptText("Value");

        Button removeButton = new Button("X");
        HBox row = new HBox(10, keyField, valueField, removeButton);
        row.setPadding(new Insets(5));

        keyField.textProperty().addListener((obs, oldV, newV) -> updateUrlFromParams());
        valueField.textProperty().addListener((obs, oldV, newV) -> updateUrlFromParams());

        removeButton.setOnAction(ev -> {
            paramsBox.getChildren().remove(row);
            updateUrlFromParams();
        });

        paramsBox.getChildren().add(row);
        updateUrlFromParams();
    }

    // ────────────────────────────── SEND REQUEST ──────────────────────────────

    private void executeRequest() {
        String method = methodComboBox.getValue();
        String url = urlField.getText().trim();

//        String fullUrl = buildUrlWithParams(baseUrl);
        Map<String, String> headers = collectHeaders();
        String body = bodyArea.getText().trim();

        responseArea.setText("Sending " + method + " request to: " + url);

        httpRequestService.sendRequest(method, url, headers, body)
            .thenAccept(response -> Platform.runLater(() -> {
                String formatted = JsonFormatter.prettyPrint(response.getBody());
                responseArea.setText(formatted);

                int statusCode = response.getStatusCode();
                String color = switch (statusCode / 100) {
                    case 2 -> "green";
                    case 3 -> "blue";
                    case 4 -> "orange";
                    case 5 -> "red";
                    default -> "black";
                };

                statusCodeLabel.setText("Status: " + statusCode);
                statusCodeLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> responseArea.setText("Error: " + ex.getMessage()));
                return null;
            });
    }

    private void updateUrlFromParams() {

        if (isSyncingParams) {
            return; // prevent recursion
        }

        isSyncingParams = true;

        try {
            String baseUrl = urlField.getText().split("\\?")[0];
            List<String> queryParams = new ArrayList<>();

            for (Node node : paramsBox.getChildren()) {
                if (node instanceof HBox hbox && hbox.getChildren().size() >= 2) {
                    TextField keyField = (TextField) hbox.getChildren().get(0);
                    TextField valueField = (TextField) hbox.getChildren().get(1);

                    String key = keyField.getText().trim();
                    String value = valueField.getText().trim();

                    if (!key.isEmpty()) {
                        queryParams.add(URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(value, StandardCharsets.UTF_8));
                    }
                }
            }

            String newUrl = queryParams.isEmpty() ? baseUrl : baseUrl + "?" + String.join("&", queryParams);
            urlField.setText(newUrl);
        } finally {
            isSyncingParams = false;
        }
    }

    private void updateParamsFromUrl(String url) {
        if (isSyncingParams) {
            return; // prevent recursion
        }

        isSyncingParams = true;
        try {
            Map<String, String> paramMap = parseQueryParams(url);
            paramsBox.getChildren().clear();

            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                addParamRow(entry.getKey(), entry.getValue());
            }
        } finally {
            isSyncingParams = false;
        }
    }

    private Map<String, String> parseQueryParams(String url) {
        Map<String, String> params = new LinkedHashMap<>();
        if (!url.contains("?")) {
            return params;
        }

        String queryPart = url.substring(url.indexOf("?") + 1);
        for (String param : queryPart.split("&")) {
            String[] pair = param.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

//    private String buildUrlWithParams(String baseUrl) {
//        List<String> queryParams = new ArrayList<>();
//        for (Node node : paramsBox.getChildren()) {
//            if (node instanceof HBox hbox && hbox.getChildren().size() >= 2) {
//                TextField keyField = (TextField) hbox.getChildren().get(0);
//                TextField valueField = (TextField) hbox.getChildren().get(1);
//                String key = keyField.getText().trim();
//                String value = valueField.getText().trim();
//
//                if (!key.isEmpty()) {
//                    queryParams.add(URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" +
//                        URLEncoder.encode(value, StandardCharsets.UTF_8));
//                }
//            }
//        }
//        return queryParams.isEmpty()
//            ? baseUrl
//            : baseUrl + (baseUrl.contains("?") ? "&" : "?") + String.join("&", queryParams);
//    }

    private Map<String, String> collectHeaders() {
        Map<String, String> headers = new HashMap<>();
        for (Node node : headersBox.getChildren()) {
            if (node instanceof HBox hbox && hbox.getChildren().size() >= 2) {
                ComboBox comboBox = (ComboBox) hbox.getChildren().get(0);
                TextField valueField = (TextField) hbox.getChildren().get(1);
                String key = comboBox.getEditor().getText().trim();
                String value = valueField.getText().trim();
                if (!key.isEmpty() && !value.isEmpty()) {
                    headers.put(key, value);
                }
            }
        }
        return headers;
    }

    // ────────────────────────────── UI HELPERS ──────────────────────────────

    private static ComboBox<String> createMethodComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("GET", "POST", "PUT", "DELETE");
        comboBox.setValue("GET");
        return comboBox;
    }

    private static TextField createUrlField() {
        TextField field = new TextField();
        field.setPromptText("Enter URL (e.g. http://example.com)");
        return field;
    }

    private static TextArea createBodyArea() {
        TextArea area = new TextArea();
        area.setPromptText("Request Body (e.g., JSON)");
        area.setPrefRowCount(10);
        return area;
    }

    private static TextArea createResponseArea() {
        TextArea area = new TextArea();
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefHeight(300);
        return area;
    }

    private void handleCopyAction() {
        String responseText = responseArea.getText();
        if (!responseText.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(responseText);
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
    }

    public static void main(String[] args) {
        launch(args);
    }
}
