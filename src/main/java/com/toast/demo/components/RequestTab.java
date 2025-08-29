package com.toast.demo.components;

import com.toast.demo.service.HttpRequestService;
import com.toast.demo.ui.RequestInputBar;
import com.toast.demo.util.JsonFormatter;
import java.util.Map;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

public class RequestTab extends Tab {

    private final HttpRequestService httpRequestService = new HttpRequestService();

    private final BodyEditor bodyEditor = new BodyEditor();
    private final HeaderEditor headerEditor = new HeaderEditor();
    private final RequestInputBar inputBar = new RequestInputBar();
    private final ParamEditor paramEditor = new ParamEditor();
    private final ResponseSection responseSection = new ResponseSection();

    public RequestTab(String title) {
        setText(title);
        TabPane requestTabs = createRequestTabs();

        VBox layout = new VBox(15, inputBar, requestTabs, responseSection);
        layout.setPadding(new Insets(10));
        setContent(layout);

        paramEditor.bindToUrlField(inputBar.getUrlField());
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

//                System.out.println("content-type: " + response.getContentType());
//                System.out.println("body: " + response.getBody().substring(0, 100));

                responseSection.setResponseBody(response.getBody(), response.getContentType());
                responseSection.setStatusCode(response.getStatusCode());
                responseSection.setResponseHeaders(response.getHeaders());


            }))
            .exceptionally(ex -> {
                Platform.runLater(() -> responseSection.setResponseBody("Error: " + ex.getMessage(), "text/plain"));
                return null;
            });
    }
}
