package com.toast.demo.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class RequestInputBar extends HBox {

    private ComboBox<String> methodComboBox = new ComboBox<>();
    private final TextField urlField = new TextField();
    private final Button sendButton = new Button("Send");
    private final Button codeButton = new Button("💻");
    private final Button saveButton = new Button("Save");

    public RequestInputBar() {
        super(10);
        setAlignment(Pos.CENTER_LEFT);
        setPadding(new Insets(5)); // Add padding for better appearance
        configureMethodDropdown();
        configureUrlField();
        configureButtons();

        getChildren().addAll(methodComboBox, urlField, sendButton, codeButton, saveButton);
    }

    private void configureMethodDropdown() {
        methodComboBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodComboBox.setValue("GET");
        methodComboBox.setPrefWidth(90);
    }

    private void configureUrlField() {
        urlField.setPromptText("Enter URL (e.g. https://example.com)");
        urlField.setPrefWidth(700);
    }

    private void configureButtons() {
        sendButton.setMinWidth(100);

        codeButton.setTooltip(new Tooltip("Show as cURL"));
        codeButton.setFocusTraversable(false); // better UX: don't highlight on tab
    }

    public String getMethod() {
        return methodComboBox.getValue();
    }

    public String getUrl() {
        return urlField.getText().trim();
    }

    public void setUrl(String url) {
        urlField.setText(url);
    }

    public void onSend(Runnable handler) {
        sendButton.setOnAction(e -> handler.run());
    }

    public TextField getUrlField() {
        return urlField;
    }

    public Button getCodeButton() {
        return codeButton;
    }

    public Button getSaveButton() {
        return saveButton;
    }

    public void setMethod(String method) {
        this.methodComboBox.setValue(method);
    }
}