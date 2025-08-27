package com.toast.demo.components;

import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

public class BodyEditor extends VBox {

    private final TextArea bodyArea;

    public BodyEditor() {
        this.bodyArea = new TextArea();
        bodyArea.setPromptText("Request Body (e.g., JSON)");
        bodyArea.setPrefRowCount(10);
        bodyArea.setWrapText(true);

        this.getChildren().add(bodyArea);
    }

    public String getBodyText() {
        return bodyArea.getText().trim();
    }

    public void setBodyText(String text) {
        bodyArea.setText(text);
    }

    public TextArea getTextArea() {
        return bodyArea;
    }
}