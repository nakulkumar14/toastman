package com.toast.demo.components;

import com.toast.demo.model.Collection;
import com.toast.demo.model.SavedRequest;
import com.toast.demo.service.CollectionsStore;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;

public class CollectionsView extends BorderPane {

    private final TreeView<String> treeView = new TreeView<>();
    private final CollectionsStore store = CollectionsStore.getInstance();

    public CollectionsView() {
        setPrefWidth(250);
        setPadding(new Insets(10));

        // toolbar
        ToolBar toolBar = new ToolBar();
        Button addButton = new Button("Add");
        Button deleteButton = new Button("Delete");
        toolBar.getItems().addAll(addButton, deleteButton);

        setCenter(treeView);

        setTop(toolBar);

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
            TreeItem<String> selected = treeView.getSelectionModel().getSelectedItem();
            if (selected != null && selected.getParent() == treeView.getRoot()) {
                String name = selected.getValue();
                store.removeCollectionByName(name);
                refresh();
            }
        });

        getStyleClass().add("collections-view");
    }

    public void refresh() {
        TreeItem<String> root = new TreeItem<>("Collections");
        root.setExpanded(true);

        for (Collection collection : store.getCollections()) {
            TreeItem<String> collectionItem = new TreeItem<>(collection.getName());
            root.getChildren().add(collectionItem);

            collection.getRequests().forEach(req ->
                collectionItem.getChildren().add(new TreeItem<>(req.getName()))
            );
        }

        treeView.setRoot(root);
    }
}
