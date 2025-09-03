package com.toast.demo.components;

import com.toast.demo.util.ToastConst;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HeaderEditor extends VBox {

    private final VBox headerRows = new VBox(5);
    private final ScrollPane scrollPane = new ScrollPane();

    public HeaderEditor() {
        setSpacing(5);
        setPadding(new Insets(5));

        setupScrollPane();
        setupAddButton();

        // Add a default row
        addHeaderRow("Content-Type", "application/json");
    }

    private void setupScrollPane() {
        scrollPane.setContent(headerRows);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        getChildren().add(scrollPane);
    }

    private void setupAddButton() {
        Button addButton = new Button("Add Header");
        addButton.setOnAction(e -> addHeaderRow("", ""));
        getChildren().add(0, addButton); // Add on top
    }

    public void addHeaderRow(String key, String value) {
        ComboBox<String> keyField = new ComboBox<>();
        keyField.setEditable(true);
        keyField.setPromptText("Header Name");
        keyField.getItems().addAll(ToastConst.COMMON_HEADERS);
        keyField.getEditor().setText(key);

        TextField valueField = new TextField(value);
        valueField.setPromptText("Header Value");

        Button removeButton = new Button("X");
        removeButton.setTooltip(new Tooltip("Remove this header"));

        HBox row = new HBox(10, keyField, valueField, removeButton);
        row.setPadding(new Insets(5));

        removeButton.setOnAction(e -> headerRows.getChildren().remove(row));

        headerRows.getChildren().add(row);
    }

    public Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();

        for (Node node : headerRows.getChildren()) {
            if (node instanceof HBox hbox && hbox.getChildren().size() >= 2) {
                ComboBox<?> comboBox = (ComboBox<?>) hbox.getChildren().get(0);
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

    public void setHeaders(Map<String, String> headers) {
        headerRows.getChildren().clear();
        headers.forEach(this::addHeaderRow);
    }
}
