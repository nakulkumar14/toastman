package com.toast.demo.components;

import com.toast.demo.util.JsonFormatter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BodyEditor extends VBox {

    private final TextArea bodyArea = new TextArea();
    private final ComboBox<String> bodyTypeComboBox = new ComboBox<>();
    private final CheckBox prettyPrintToggle = new CheckBox("Pretty Print");
    private final Label errorLabel = new Label();

    // form-data UI
    private final TableView<Map.Entry<String, String>> formTable = new TableView<>();
    private final ObservableList<Entry<String, String>> formItems = FXCollections.observableArrayList();
    private final Button addRowBtn = new Button("+");

    private String lastRawBody = "";

    public BodyEditor() {
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));

        bodyTypeComboBox.getItems().addAll("JSON", "XML", "Form Data", "Plain Text");
        bodyTypeComboBox.setValue("JSON");

        bodyArea.setPrefRowCount(10);
        bodyArea.setPromptText("Request Body (e.g., JSON)");
        bodyArea.setWrapText(true);

        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);

        setupFormTable();

        addRowBtn.setOnAction(e -> formItems.add(Map.entry("key", "value")));
        addRowBtn.setVisible(false);

        HBox topControls = new HBox(10, new Label("Body Type:"), bodyTypeComboBox, prettyPrintToggle);
        topControls.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(topControls, bodyArea, formTable, errorLabel);

        bodyTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> switchTo(newVal));

    }

    private void setupFormTable() {
        TableColumn<Map.Entry<String, String>, String> keyCol = new TableColumn<>("Key");
        keyCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKey()));
        keyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        keyCol.setOnEditCommit(e -> {
            Map.Entry<String, String> entry = e.getRowValue();
            int idx = formItems.indexOf(entry);
            formItems.set(idx, new AbstractMap.SimpleEntry<>(e.getNewValue(), entry.getValue()));
        });

        TableColumn<Map.Entry<String, String>, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getValue()));
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(e -> {
            Map.Entry<String, String> entry = e.getRowValue();
            int idx = formItems.indexOf(entry);
            formItems.set(idx, new AbstractMap.SimpleEntry<>(entry.getKey(), e.getNewValue()));

        });

        formTable.getColumns().addAll(keyCol, valueCol);
        formTable.setItems(formItems);
        formTable.setEditable(true);
    }

    private void switchTo(String type) {
        this.getChildren().removeIf(node -> node == bodyArea || node == formTable);

        if ("Form Data".equalsIgnoreCase(type)) {
            this.getChildren().add(1, formTable);
            addRowBtn.setVisible(true);
        } else {
            this.getChildren().add(1, bodyArea);
            addRowBtn.setVisible(false);
        }
    }


    private void setupListeners() {
        prettyPrintToggle.selectedProperty()
            .addListener((obs, wasSelected, isSelected) -> togglePrettyPrint(isSelected));

        // Validate JSON format on body change if JSON is selected
        ChangeListener<String> validationListener = (obs, oldVal, newVal) -> validateJsonIfNeeded(newVal);

        bodyArea.textProperty().addListener(validationListener);
        bodyTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Re-validate when body type changes
            validateJsonIfNeeded(bodyArea.getText());
        });
    }

    private void togglePrettyPrint(boolean enablePrettyPrint) {
        if (!"JSON".equalsIgnoreCase(getBodyType())) {
            return; // pretty print only for JSON
        }

        if (enablePrettyPrint) {
            String formatted = JsonFormatter.prettyPrint(bodyArea.getText());
            if (formatted != null) {
                lastRawBody = bodyArea.getText();
                bodyArea.setText(formatted);
            } else {
                showValidationError("Invalid JSON format");
                prettyPrintToggle.setSelected(false);
            }
        } else {
            if (!lastRawBody.isEmpty()) {
                bodyArea.setText(lastRawBody);
            }
        }
    }

    private void validateJsonIfNeeded(String text) {
        if (!"JSON".equalsIgnoreCase(getBodyType())) {
            hideValidationError();
            return;
        }

        boolean valid = JsonFormatter.isValidJson(text);
        if (valid) {
            hideValidationError();
        } else {
            showValidationError("Invalid JSON format");
        }
    }

    private void showValidationError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        bodyArea.setStyle("-fx-border-color: red;");
    }

    private void hideValidationError() {
        errorLabel.setVisible(false);
        bodyArea.setStyle("");
    }

    public String getBodyType() {
        return bodyTypeComboBox.getValue();
    }

    public String getBodyText() {
        if ("Form Data".equalsIgnoreCase(getBodyType())) {
            return formItems.stream()
                .map(entry -> URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) +
                    "=" +
                    URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        }
        return bodyArea.getText().trim();
    }

    public void setBodyText(String body) {
        if (body == null || body.isBlank()) {
            bodyArea.clear();
            formItems.clear();
            return;
        }

        if ("Form Data".equalsIgnoreCase(getBodyType())
            || body.contains("=") && body.contains("&")) {
            try {
                Map<String, String> parsed = Arrays.stream(body.split("&"))
                    .map(pair -> pair.split("=", 2))
                    .filter(arr -> arr.length == 2)
                    .collect(Collectors.toMap(
                        arr -> URLDecoder.decode(arr[0], StandardCharsets.UTF_8),
                        arr -> URLDecoder.decode(arr[1], StandardCharsets.UTF_8),
                        (a, b) -> b,
                        LinkedHashMap::new
                    ));

                bodyTypeComboBox.setValue("Form Data"); // switch automatically
                setFormBody(parsed);
                return;
            } catch (Exception e) {
                // fallback to plain text if parsing fails
                bodyTypeComboBox.setValue("Plain Text");
                bodyArea.setText(body);
                return;
            }
        }

        // Fallback for non-form-data
        bodyArea.setText(body);
        lastRawBody = body;

        if ("JSON".equalsIgnoreCase(getBodyType())) {
            prettyPrintToggle.setSelected(true);
            togglePrettyPrint(true);
        }
    }

    public Map<String, String> getFormBody() {
        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : formItems) {
            map.put(e.getKey(), e.getValue());
        }
        return map;
    }

    public void setFormBody(Map<String, String> formData) {
        formItems.setAll(formData.entrySet());
        bodyTypeComboBox.setValue("Form Data");
        switchTo("Form Data");
    }
}