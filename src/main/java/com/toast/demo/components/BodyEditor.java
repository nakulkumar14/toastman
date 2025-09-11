package com.toast.demo.components;

import com.toast.demo.util.JsonFormatter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
    private final ObservableList<Map.Entry<String, String>> formItems = FXCollections.observableArrayList();
    private final Button addRowBtn = new Button("+");

    private final HBox topControls = new HBox(10);

    private String lastRawBody = "";

    public BodyEditor() {
        setupUI();
        setupFormTable();
        setupListeners();

        // initial layout
        this.getChildren().clear();
        this.getChildren().add(topControls);
        switchTo(bodyTypeComboBox.getValue());
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

        addRowBtn.setOnAction(e -> formItems.add(new AbstractMap.SimpleEntry<>("", "")));
        addRowBtn.setMaxWidth(40);

        topControls.getChildren().setAll(new Label("Body Type:"), bodyTypeComboBox, prettyPrintToggle, addRowBtn);
        topControls.setAlignment(Pos.CENTER_LEFT);
    }

    private void setupFormTable() {
        formTable.setEditable(true);

        TableColumn<Map.Entry<String, String>, String> keyCol = createEditableColumn("Key", Map.Entry::getKey,
            (entry, newVal) -> new AbstractMap.SimpleEntry<>(newVal, entry.getValue()));

        TableColumn<Map.Entry<String, String>, String> valueCol = createEditableColumn("Value", Map.Entry::getValue,
            (entry, newVal) -> new AbstractMap.SimpleEntry<>(entry.getKey(), newVal));

        TableColumn<Map.Entry<String, String>, Void> removeCol = new TableColumn<>("");
        removeCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🗑");

            {
                btn.setOnAction(e -> formItems.remove(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
        removeCol.setPrefWidth(40);

        formTable.getColumns().setAll(keyCol, valueCol, removeCol);
        formTable.setItems(formItems);
    }


    private TableColumn<Map.Entry<String, String>, String> createEditableColumn(
        String title,
        java.util.function.Function<Map.Entry<String, String>, String> getter,
        java.util.function.BiFunction<Map.Entry<String, String>, String, Map.Entry<String, String>> updater) {

        TableColumn<Map.Entry<String, String>, String> col = new TableColumn<>(title);
        col.setCellValueFactory(data -> new SimpleStringProperty(getter.apply(data.getValue())));
        col.setCellFactory(TextFieldTableCell.forTableColumn());
        col.setOnEditCommit(e -> {
            int idx = e.getTablePosition().getRow();
            formItems.set(idx, updater.apply(e.getRowValue(), e.getNewValue()));
        });
        return col;
    }

    private void setupListeners() {
        // switch UI when body type changes
        bodyTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> switchTo(newVal));

        prettyPrintToggle.selectedProperty()
            .addListener((obs, wasSelected, isSelected) -> togglePrettyPrint(isSelected));

        // Validate JSON when text changes and type is JSON
        ChangeListener<String> validationListener = (obs, oldVal, newVal) -> validateJsonIfNeeded(newVal);
        bodyArea.textProperty().addListener(validationListener);
    }

    private void switchTo(String type) {
        // keep topControls at index 0, replace the rest
        List<Node> nodes = new ArrayList<>();
        nodes.add(topControls);

        if ("Form Data".equalsIgnoreCase(type)) {
            nodes.add(formTable);
            nodes.add(addRowBtn);
            nodes.add(errorLabel);
        } else {
            nodes.add(bodyArea);
            nodes.add(errorLabel);
        }

        this.getChildren().setAll(nodes);
        // show/hide add button depending on mode
        addRowBtn.setVisible("Form Data".equalsIgnoreCase(type));
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

    /**
     * Set raw body text. If the text looks like urlencoded form data (a=1&b=2) we automatically switch to Form Data and
     * populate the table.
     */
    public void setBodyText(String body) {
        if (body == null || body.isBlank()) {
            bodyArea.clear();
            formItems.clear();
            return;
        }

        // Simple heuristic: looks like key=value pairs joined by &
        boolean looksLikeForm = body.contains("=") && body.contains("&");
        if (looksLikeForm) {
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
                setFormBody(parsed);
                return;
            } catch (Exception e) {
                // fallback to plain text if parsing fails
            }
        }

        // fallback: plain/raw text
        bodyTypeComboBox.setValue("Plain Text");
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
        List<Entry<String, String>> toAdd = formData.entrySet().stream()
            .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue()))
            .collect(Collectors.toList());
        formItems.setAll(toAdd);
        bodyTypeComboBox.setValue("Form Data");
        switchTo("Form Data");
    }
}