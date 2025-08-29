package com.toast.demo.components;

import com.toast.demo.service.HttpRequestService;
import com.toast.demo.ui.RequestInputBar;
import com.toast.demo.util.CurlGenerator;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RequestTab extends Tab {

    private final HttpRequestService httpRequestService = new HttpRequestService();

    private final BodyEditor bodyEditor = new BodyEditor();
    private final HeaderEditor headerEditor = new HeaderEditor();
    private final RequestInputBar inputBar = new RequestInputBar();
    private final ParamEditor paramEditor = new ParamEditor();
    private final ResponseSection responseSection = new ResponseSection();
    private final CurlPane curlPane = new CurlPane();
    private final SplitPane splitPane = new SplitPane();


    public RequestTab(String title) {
        setText(title);
        TabPane requestTabs = createRequestTabs();

        VBox layout = new VBox(15, inputBar, requestTabs, responseSection);
        layout.setPadding(new Insets(10));

        splitPane.getItems().addAll(layout, curlPane);
        splitPane.setDividerPositions(0.75); // 75% editor, 25% curl pane

        // Optional: Hide divider when curl pane is hidden
        curlPane.visibleProperty().addListener((obs, oldVal, visible) -> {
            if (visible && !splitPane.getItems().contains(curlPane)) {
                splitPane.getItems().add(curlPane);
                splitPane.setDividerPositions(0.75);
            } else if (!visible) {
                splitPane.getItems().remove(curlPane);
            }
        });

        setContent(splitPane);

        paramEditor.bindToUrlField(inputBar.getUrlField());
//        inputBar.getCodeButton().setOnAction(e -> showCurlPopup());
        inputBar.getCodeButton().setOnAction(e -> toggleCurlPane());
        curlPane.setOnClose(() -> curlPane.setVisible(false));

        inputBar.onSend(this::executeRequest);
    }

    private TabPane createRequestTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab paramsTab = new Tab("Params", paramEditor);

        VBox headersTabContent = new VBox(headerEditor);
        Tab headersTab = new Tab("Headers", headersTabContent);
        Tab bodyTab = new Tab("Body", bodyEditor);

        tabPane.getTabs().addAll(paramsTab, headersTab, bodyTab);
        return tabPane;
    }

    private void executeRequest() {
        String method = inputBar.getMethod();
        String url = inputBar.getUrl();

        Map<String, String> headers = headerEditor.getHeaders(); //collectHeaders();
        String body = bodyEditor.getBodyText();

        responseSection.setResponseBody("Sending " + method + " request to: " + url, "");

        httpRequestService.sendRequest(method, url, headers, body)
            .thenAccept(response -> Platform.runLater(() -> {

                responseSection.setResponseBody(response.getBody(), response.getContentType());
                responseSection.setStatusCode(response.getStatusCode());
                responseSection.setResponseHeaders(response.getHeaders());

                if (curlPane.isVisible()) {
                    String updatedCurl = CurlGenerator.generateCurl(method, url, headers, body);
                    curlPane.setCurlCommand(updatedCurl);
                }

            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> responseSection.setResponseBody("Error: " + ex.getMessage(), "text/plain"));
                return null;
            });
    }

//    private void showCurlPopup() {
//        String method = inputBar.getMethod();
//        String url = inputBar.getUrl();
//        Map<String, String> headers = headerEditor.getHeaders();
//        String body = bodyEditor.getBodyText(); // assuming you modularized BodyEditor
//
//        String curlCommand = CurlGenerator.generateCurl(method, url, headers, body);
//
//        TextArea curlArea = new TextArea(curlCommand);
//        curlArea.setWrapText(true);
//        curlArea.setEditable(false);
//        curlArea.setPrefRowCount(8);
//
//        VBox popupLayout = getPopupLayout(curlCommand, curlArea);
//
//        Stage popup = new Stage();
//        popup.setTitle("cURL Preview");
//        popup.setScene(new Scene(popupLayout, 800, 300));
//        popup.initOwner(inputBar.getScene().getWindow());
//        popup.show();
//    }

    private void toggleCurlPane() {
        if (!curlPane.isVisible()) {
            String method = inputBar.getMethod();
            String url = inputBar.getUrl();
            Map<String, String> headers = headerEditor.getHeaders();
            String body = bodyEditor.getBodyText();
            String curlCommand = CurlGenerator.generateCurl(method, url, headers, body);

            curlPane.setCurlCommand(curlCommand);
            curlPane.setVisible(true);
        } else {
            curlPane.setVisible(false);
        }
    }

//    private static VBox getPopupLayout(String curlCommand, TextArea curlArea) {
//        Button copyButton = new Button("Copy");
//        copyButton.setOnAction(ev -> {
//            ClipboardContent content = new ClipboardContent();
//            content.putString(curlCommand);
//            Clipboard.getSystemClipboard().setContent(content);
//            copyButton.setText("Copied!");
//            copyButton.setDisable(true);
//            new Thread(() -> {
//                try {
//                    Thread.sleep(800);
//                } catch (InterruptedException ignored) {
//                }
//                Platform.runLater(() -> {
//                    copyButton.setText("Copy");
//                    copyButton.setDisable(false);
//                });
//            }).start();
//        });
//
//        VBox popupLayout = new VBox(10, new Label("Generated cURL Command:"), curlArea, copyButton);
//        popupLayout.setPadding(new Insets(15));
//        return popupLayout;
//    }

}
