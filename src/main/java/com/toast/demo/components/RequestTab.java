package com.toast.demo.components;

import com.toast.demo.model.Collection;
import com.toast.demo.model.SavedRequest;
import com.toast.demo.service.CollectionsStore;
import com.toast.demo.service.HttpRequestService;
import com.toast.demo.ui.RequestInputBar;
import com.toast.demo.util.CurlGenerator;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class RequestTab extends Tab {

    private final HttpRequestService httpRequestService = new HttpRequestService();

    private final BodyEditor bodyEditor = new BodyEditor();
    private final HeaderEditor headerEditor = new HeaderEditor();
    private final RequestInputBar inputBar = new RequestInputBar();
    private final ParamEditor paramEditor = new ParamEditor();
    private final ResponseSection responseSection = new ResponseSection();
    private final CurlPane curlPane = new CurlPane();
    private final SplitPane splitPane = new SplitPane();

    private String savedRequestId;


    public RequestTab(String title) {
        setText(title);
        initializeUI();
        bindInteractions();
    }

    private void initializeUI() {
        VBox layout = new VBox(15, inputBar, createRequestTabs(), responseSection);
        layout.setPadding(new Insets(10));

        splitPane.getItems().add(layout);
        splitPane.setDividerPositions(0.75);
        setContent(splitPane);

        curlPane.setVisible(false); // hidden by default
    }

    private void bindInteractions() {
        paramEditor.bindToUrlField(inputBar.getUrlField());

        inputBar.onSend(this::executeRequest);
        inputBar.getCodeButton().setOnAction(e -> toggleCurlPane());
        inputBar.getSaveButton().setOnAction(e -> saveToCollection());

        curlPane.setOnClose(() -> curlPane.setVisible(false));

        // Optional: Hide divider when curl pane is hidden
        curlPane.visibleProperty().addListener((obs, oldVal, isVisible) -> {
            if (isVisible && !splitPane.getItems().contains(curlPane)) {
                splitPane.getItems().add(curlPane);
                splitPane.setDividerPositions(0.75);
            } else if (!isVisible) {
                splitPane.getItems().remove(curlPane);
            }
        });
    }

    private TabPane createRequestTabs() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab paramsTab = new Tab("Params", paramEditor);
        Tab headersTab = new Tab("Headers", new VBox(headerEditor));
        Tab bodyTab = new Tab("Body", bodyEditor);

        tabPane.getTabs().addAll(paramsTab, headersTab, bodyTab);
        return tabPane;
    }

    private void executeRequest() {
        String method = inputBar.getMethod();
        String url = inputBar.getUrl();
        Map<String, String> headers = headerEditor.getHeaders();

        String body = null;
        if ("x-www-form-urlencoded".equals(bodyEditor.getBodyType())) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
            body = bodyEditor.getBodyText();
        } else {
            body = bodyEditor.getBodyText();
        }
        String finalBody = body; // for use in lambda

        responseSection.setResponseBody("Sending " + method + " request to: " + url, "");

        httpRequestService.sendRequest(method, url, headers, body)
            .thenAccept(response -> Platform.runLater(() -> {

                responseSection.setResponseBody(response.getBody(), response.getContentType());
                responseSection.setStatusCode(response.getStatusCode());
                responseSection.setResponseHeaders(response.getHeaders());

                if (curlPane.isVisible()) {
                    String updatedCurl = CurlGenerator.generateCurl(method, url, headers, finalBody);
                    curlPane.setCurlCommand(updatedCurl);
                }
            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> responseSection.setResponseBody("Error: " + ex.getMessage(), "text/plain"));
                return null;
            });
    }

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

    private void saveToCollection() {

        Dialog<SavedRequest> dialog = new Dialog<>();
        dialog.setTitle("Save Request");

        Label nameLabel = new Label("Request name:");
        TextField nameField = new TextField();

        Label collectionLabel = new Label("Collection:");
        ComboBox<String> collectionBox = new ComboBox<>();
        collectionBox.getItems().addAll(CollectionsStore.getInstance()
            .getCollections().stream()
            .map(Collection::getName)
            .toList());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(collectionLabel, 0, 1);
        grid.add(collectionBox, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                String reqName = nameField.getText();
                String collection = collectionBox.getValue();

                if (reqName != null && !reqName.isBlank() && collection != null) {
                    return new SavedRequest(
                        reqName,
                        inputBar.getMethod(),
                        inputBar.getUrl(),
                        headerEditor.getHeaders(),
                        bodyEditor.getBodyText()
                    );
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(savedRequest -> {
            String collectionName = collectionBox.getValue();
            CollectionsStore.getInstance().addRequestToCollection(collectionName, savedRequest);
        });
    }

    public RequestInputBar getInputBar() {
        return inputBar;
    }

    public HeaderEditor getHeaderEditor() {
        return headerEditor;
    }

    public BodyEditor getBodyEditor() {
        return bodyEditor;
    }

    public String getSavedRequestId() {
        return savedRequestId;
    }

    public void setSavedRequestId(String id) {
        this.savedRequestId = id;
    }

}
