package com.toast.demo.components;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ParamEditor extends VBox {

    private final VBox paramRows = new VBox(5);
    private boolean isSyncing = false;
    private TextField boundUrlField;

    public ParamEditor() {
        setSpacing(5);
        setPadding(new Insets(5));

        Button addButton = new Button("Add Param");
        addButton.setOnAction(e -> addParamRow("", ""));

        ScrollPane scrollPane = new ScrollPane(paramRows);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);

        getChildren().addAll(addButton, scrollPane);
    }

    public void bindToUrlField(TextField urlField) {
        this.boundUrlField = urlField;

        // Sync when URL changes
        ChangeListener<String> urlListener = (obs, oldVal, newVal) -> {
            if (isSyncing) {
                return;
            }
            updateParamsFromUrl(newVal);
        };
        urlField.textProperty().addListener(urlListener);
    }

    private void updateParamsFromUrl(String url) {
        isSyncing = true;
        try {
            Map<String, String> paramMap = parseQueryParams(url);
            paramRows.getChildren().clear();
            for (Map.Entry<String, String> entry : paramMap.entrySet()) {
                addParamRow(entry.getKey(), entry.getValue());
            }
        } finally {
            isSyncing = false;
        }
    }

    private Map<String, String> parseQueryParams(String url) {
        Map<String, String> params = new LinkedHashMap<>();
        if (!url.contains("?")) {
            return params;
        }

        String query = url.substring(url.indexOf("?") + 1);
        for (String param : query.split("&")) {
            String[] pair = param.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = pair.length > 1 ? URLDecoder.decode(pair[1], StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }
        return params;
    }

    public void addParamRow(String key, String value) {
        TextField keyField = new TextField(key);
        keyField.setPromptText("Key");

        TextField valueField = new TextField(value);
        valueField.setPromptText("Value");

        Button removeButton = new Button("X");
        HBox row = new HBox(10, keyField, valueField, removeButton);
        row.setPadding(new Insets(5));

        keyField.textProperty().addListener((obs, oldV, newV) -> updateUrlFromParams());
        valueField.textProperty().addListener((obs, oldV, newV) -> updateUrlFromParams());

        removeButton.setOnAction(ev -> {
            paramRows.getChildren().remove(row);
            updateUrlFromParams();
        });

        paramRows.getChildren().add(row);
        updateUrlFromParams();
    }

    private void updateUrlFromParams() {
        if (isSyncing || boundUrlField == null) {
            return;
        }

        isSyncing = true;
        try {
            String baseUrl = boundUrlField.getText().split("\\?")[0];
            List<String> paramList = new ArrayList<>();

            for (Node node : paramRows.getChildren()) {
                if (node instanceof HBox hbox && hbox.getChildren().size() >= 2) {
                    TextField keyField = (TextField) hbox.getChildren().get(0);
                    TextField valueField = (TextField) hbox.getChildren().get(1);
                    String key = keyField.getText().trim();
                    String value = valueField.getText().trim();

                    if (!key.isEmpty()) {
                        paramList.add(URLEncoder.encode(key, StandardCharsets.UTF_8) + "=" +
                            URLEncoder.encode(value, StandardCharsets.UTF_8));
                    }
                }
            }

            String newUrl = paramList.isEmpty() ? baseUrl : baseUrl + "?" + String.join("&", paramList);
            Platform.runLater(() -> boundUrlField.setText(newUrl));
        } finally {
            isSyncing = false;
        }
    }
}