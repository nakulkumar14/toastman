package com.toast.demo;

import com.toast.demo.components.BodyEditor;
import com.toast.demo.components.HeaderEditor;
import com.toast.demo.components.ParamEditor;
import com.toast.demo.components.ResponseSection;
import com.toast.demo.service.HttpRequestService;
import com.toast.demo.ui.RequestInputBar;
import com.toast.demo.util.JsonFormatter;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HttpRequestUI extends Application {

    private final HttpRequestService httpRequestService = new HttpRequestService();

    private final BodyEditor bodyEditor = new BodyEditor();

    private HeaderEditor headerEditor = new HeaderEditor();
    private RequestInputBar inputBar = new RequestInputBar();
    private ParamEditor paramEditor = new ParamEditor();
    private ResponseSection responseSection = new ResponseSection();


    @Override
    public void start(Stage primaryStage) {

        TabPane requestTabs = createRequestTabs();

        VBox layout = new VBox(15, inputBar, requestTabs, responseSection);

        layout.setPadding(new Insets(20));
        inputBar.onSend(() -> executeRequest());

        paramEditor.bindToUrlField(inputBar.getUrlField());  // sync with URL field

        Scene scene = new Scene(layout, 1000, 750);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("HTTP Request Sender");
        primaryStage.show();
    }
    // ────────────────────────────── UI SECTIONS ──────────────────────────────

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

    // ────────────────────────────── SEND REQUEST ──────────────────────────────

    private void executeRequest() {
        String method = inputBar.getMethod();
        String url = inputBar.getUrl();

        Map<String, String> headers = headerEditor.getHeaders(); //collectHeaders();
        String body = bodyEditor.getBodyText();

        responseSection.setResponseBody("Sending " + method + " request to: " + url);

        httpRequestService.sendRequest(method, url, headers, body)
            .thenAccept(response -> Platform.runLater(() -> {
                String formatted = JsonFormatter.prettyPrint(response.getBody());

                responseSection.setResponseBody(formatted);
                responseSection.setStatusCode(response.getStatusCode());

            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> responseSection.setResponseBody("Error: " + ex.getMessage()));
                return null;
            });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
