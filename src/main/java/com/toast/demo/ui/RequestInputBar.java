package com.toast.demo.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;

public class RequestInputBar extends HBox {

    private final ComboBox<String> methodComboBox = new ComboBox<>();
    private final TextField urlField = new TextField();
    private final Button sendButton = new Button("Send");
    private final Button codeButton = new Button("💻");

    public RequestInputBar() {
        super(10);
        setAlignment(Pos.CENTER_LEFT);

        methodComboBox.getItems().addAll("GET", "POST", "PUT", "DELETE", "PATCH");
        methodComboBox.setValue("GET");

        urlField.setPromptText("Enter URL (e.g. http://example.com)");
        urlField.setPrefWidth(700);

        sendButton.setMinWidth(100);

        codeButton.setTooltip(new Tooltip("Show as cURL"));

//        HBox bar = new HBox(10, methodComboBox, urlField, sendButton, codeButton);
//        bar.setAlignment(Pos.CENTER_LEFT);

        getChildren().addAll(methodComboBox, urlField, sendButton, codeButton);
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
}