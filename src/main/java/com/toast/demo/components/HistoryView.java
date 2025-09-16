package com.toast.demo.components;

import com.toast.demo.RequestTabPane;
import com.toast.demo.model.SavedRequest;
import com.toast.demo.service.RequestHistory;
import javafx.geometry.Insets;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;

public class HistoryView extends BorderPane {

    private final ListView<SavedRequest> listView = new ListView<>();
    private final RequestTabPane tabPane;

    public HistoryView(RequestTabPane tabPane) {
        this.tabPane = tabPane;
        setPrefWidth(250);
        setPadding(new Insets(10));

        setCenter(listView);

        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(SavedRequest req, boolean empty) {
                super.updateItem(req, empty);
                if (empty || req == null) {
                    setText(null);
                } else {
                    setText(req.getMethod() + " " + req.getUrl());
                }
            }
        });

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                SavedRequest req = listView.getSelectionModel().getSelectedItem();
                if (req != null) {
//                    tabPane.openRequest(req); // you’d implement this

                    RequestTab tab = new RequestTab(req.getName());

                    // pre-fill values
                    tab.getInputBar().setUrl(req.getUrl());
                    tab.getInputBar().setMethod(req.getMethod());
                    tab.getHeaderEditor().setHeaders(req.getHeaders());
                    tab.getBodyEditor().setBodyText(req.getBody());

                    tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab); // before the + tab
                    tabPane.getSelectionModel().select(tab);
                }
            }
        });

        refresh();
    }

    public void refresh() {
        listView.getItems().setAll(RequestHistory.getInstance().getAll());
    }

    public ListView<SavedRequest> getListView() {
        return listView;
    }
}