package com.toast.demo.components;

import com.toast.demo.RequestTabPane;
import com.toast.demo.model.Collection;
import com.toast.demo.model.SavedRequest;
import com.toast.demo.service.CollectionsStore;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

public class CollectionsView extends BorderPane {

    private final TreeView<Object> treeView = new TreeView<>();
    private final CollectionsStore store = CollectionsStore.getInstance();
    private final RequestTabPane tabPane;


    public CollectionsView(RequestTabPane tabPane) {
        this.tabPane = tabPane;
        setPrefWidth(250);
        setPadding(new Insets(10));

        // toolbar
        ToolBar toolBar = new ToolBar();
        Button addButton = new Button("Add");
        Button deleteButton = new Button("Delete");
        toolBar.getItems().addAll(addButton, deleteButton);

        setCenter(treeView);

        setTop(toolBar);

        // Listen to store changes
        store.addListener(updatedCollections -> {
            Platform.runLater(this::refresh);
        });

        // List of saved collection
        refresh();

        // add collection
        addButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("New Collection");
            dialog.setHeaderText("Enter collection name:");
            dialog.setTitle("Add Collection");
            dialog.showAndWait().ifPresent(name -> {
                if (!name.isBlank()) {
                    store.addCollection(new Collection(name));
                    refresh();
                }
            });
        });

        // delete collection
        deleteButton.setOnAction(e -> {
            TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getValue() instanceof Collection collection) {
                store.removeCollectionByName(collection.getName());
                refresh();
            }
        });

        treeView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
                if (selected != null && selected.getValue() instanceof SavedRequest req) {
                    openRequestInNewTab(req);
                }
            }
        });

        getStyleClass().add("collections-view");
    }

    public void refresh() {
        TreeItem<Object> root = new TreeItem<>("Collections");
        root.setExpanded(true);

        for (Collection col : store.getCollections()) {
            TreeItem<Object> collectionItem = new TreeItem<>(col);
            for (SavedRequest req : col.getRequests()) {
                collectionItem.getChildren().add(new TreeItem<>(req));
            }
            root.getChildren().add(collectionItem);
        }

        treeView.setRoot(root);
        treeView.setShowRoot(false);

        // custom cell factory: display names
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof Collection c) {
                    setText(c.getName());
                } else if (item instanceof SavedRequest r) {
                    setText(r.getName());
                } else {
                    setText(item.toString());
                }
            }
        });
    }

    private void openRequestInNewTab(SavedRequest req) {
        // 1. Check if already open
        for (Tab existingTab : tabPane.getTabs()) {
            if (existingTab instanceof RequestTab rt) {
                if (req.getId().equals(rt.getSavedRequestId())) {
                    tabPane.getSelectionModel().select(existingTab);
                    return; // already open, just focus it
                }
            }
        }

        // 2. Create new tab
        RequestTab tab = new RequestTab(req.getName());
        tab.setSavedRequestId(req.getId()); // link request to tab

        // pre-fill values
        tab.getInputBar().setUrl(req.getUrl());
        tab.getInputBar().setMethod(req.getMethod());
        tab.getHeaderEditor().setHeaders(req.getHeaders());
        tab.getBodyEditor().setBodyText(req.getBody());

        tabPane.getTabs().add(tabPane.getTabs().size() - 1, tab); // before the + tab
        tabPane.getSelectionModel().select(tab);
    }
}
