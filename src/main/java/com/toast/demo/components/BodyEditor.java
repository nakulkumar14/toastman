package com.toast.demo.components;

import com.toast.demo.util.JsonFormatter;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class BodyEditor extends VBox {

    private final TextArea bodyArea = new TextArea();
    private final ComboBox<String> bodyTypeComboBox = new ComboBox<>();
    private final CheckBox prettyPrintToggle = new CheckBox("Pretty Print");
    private final Label errorLabel = new Label();

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

        HBox topControls = new HBox(10, new Label("Body Type:"), bodyTypeComboBox, prettyPrintToggle);
        topControls.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(topControls, bodyArea, errorLabel);
    }

    private void setupListeners() {
        prettyPrintToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> togglePrettyPrint(isSelected));

        // Validate JSON format on body change if JSON is selected
        ChangeListener<String> validationListener = (obs, oldVal, newVal) -> validateJsonIfNeeded(newVal);

        bodyArea.textProperty().addListener(validationListener);
        bodyTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            // Re-validate when body type changes
            validateJsonIfNeeded(bodyArea.getText());
        });
    }

    private void togglePrettyPrint(boolean enablePrettyPrint) {
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
        return bodyArea.getText().trim();
    }

    public void setBodyText(String body) {
        bodyArea.setText(body);
        lastRawBody = body;
        prettyPrintToggle.setSelected(true);
        togglePrettyPrint(true);
    }
}