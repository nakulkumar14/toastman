package com.toast.demo.components;

import com.toast.demo.RequestTabPane;
import com.toast.demo.model.Collection;
import com.toast.demo.model.SavedRequest;
import com.toast.demo.service.CollectionsStore;
import com.toast.demo.util.CurlParser;
import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionsView extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(CollectionsView.class);

    private final TreeView<Object> treeView = new TreeView<>();
    private final CollectionsStore store = CollectionsStore.getInstance();
    private final RequestTabPane tabPane;

    private final Button addButton = new Button("Add");
    private final Button deleteButton = new Button("Delete");
    private final Button importButton = new Button("Import cURL");

    private final Button importCollectionButton = new Button("Import Collection");

    private final TextField searchField = new TextField();
    private final Button clearBtn = new Button("❌");

    public CollectionsView(RequestTabPane tabPane) {
        this.tabPane = tabPane;
        setPrefWidth(250);
        setPadding(new Insets(10));

        setupToolbar();
        setupTreeView();
        setupButtonActions();
        setupImportCollectionAction();

        // Listen to store changes
        store.addListener(updatedCollections -> Platform.runLater(this::refresh));

        // List of saved collection
        refresh();

        getStyleClass().add("collections-view");
    }

    private void setupToolbar() {
        ToolBar toolBar = new ToolBar(addButton, deleteButton, importButton, importCollectionButton);
        VBox topBox = new VBox(5, toolBar, buildSearchBox());
        topBox.setPadding(new Insets(5));
        setTop(topBox);
        // main tree view stays in center
        setCenter(treeView);
    }

    private HBox buildSearchBox() {
        searchField.setPromptText("Search requests...");
        clearBtn.setVisible(false);
        clearBtn.setOnAction(e -> searchField.clear());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            clearBtn.setVisible(newVal != null && !newVal.isBlank());
            filterSearch(newVal);
        });

        HBox searchBox = new HBox(5, searchField, clearBtn);
        searchBox.setAlignment(Pos.CENTER_LEFT);
        searchBox.setPadding(new Insets(5));
        return searchBox;
    }

    private void setupTreeView() {
        treeView.setShowRoot(false);
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

        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Object val = getSelectedValue();
                if (val instanceof SavedRequest req) {
                    openRequestInNewTab(req);
                }
            }
        });

        treeView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                deleteSelected();
            }
        });
    }

    private void setupButtonActions() {
        // add collection
        addButtonAction();
        // delete collection
        deleteButtonAction();
        importButtonAction();
    }

    private void importButtonAction() {
        importButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Import from cURL");
            dialog.setHeaderText("Paste your cURL command below:");
            dialog.getEditor().setPrefWidth(600);

            dialog.showAndWait().ifPresent(curl -> {
                try {
                    SavedRequest req = CurlParser.parseCurl(curl);

                    // Save to first collection for now
                    CollectionsStore.getInstance()
                        .addRequestToCollection("Default", req);
                } catch (Exception ex) {
                    log.error("Failed to parse cURL", ex);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to parse cURL:\n" + ex.getMessage());
                }
            });
        });
    }

    private void deleteButtonAction() {
        deleteButton.setOnAction(e -> deleteSelected());
    }

    private void addButtonAction() {
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
    }

    private void filterSearch(String query) {
        treeView.setRoot(buildTree(query));
    }

    public void refresh() {
        treeView.setRoot(buildTree(null));
    }

    private TreeItem<Object> buildTree(String query) {
        TreeItem<Object> root = new TreeItem<>("Collections");
        root.setExpanded(true);
        String q = query == null ? null : query.toLowerCase();

        for (Collection col : store.getCollections()) {
            TreeItem<Object> colItem = new TreeItem<>(col);
            for (SavedRequest req : col.getRequests()) {
                if (q == null ||
                    req.getName().toLowerCase().contains(q) ||
                    req.getUrl().toLowerCase().contains(q) ||
                    col.getName().toLowerCase().contains(q)) {
                    colItem.getChildren().add(new TreeItem<>(req));
                }
            }
            if (!colItem.getChildren().isEmpty()) {
                root.getChildren().add(colItem);
            }
        }
        return root;
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

    // Add to your CollectionsView class
    private void setupImportCollectionAction() {
        importCollectionButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Import Collection");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files (*.json)", "*.json"));
            File file = fileChooser.showOpenDialog(getScene().getWindow());

            if (file != null) {
                try {
                    // Parse the JSON into a Collection object
                    // You'll need a way to deserialize the JSON. A library like Gson or Jackson is ideal.
                    Collection importedCollection = CollectionImporter.importCollection(file);
                    log.debug("Imported collection: {}", importedCollection.getName());
                    CollectionsStore.getInstance().addCollection(importedCollection);

                    refresh();

                    showAlert(Alert.AlertType.INFORMATION, "Success", "The file was saved successfully.");
                } catch (IOException | RuntimeException ex) {
                    log.error("Failed to import collection", ex);
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to import collection: " + ex.getMessage());
                }
            }
        });
    }

    private Object getSelectedValue() {
        TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
        return selected != null ? selected.getValue() : null;
    }

    private void deleteSelected() {
        Object val = getSelectedValue();
        if (val instanceof Collection col) {
            store.removeCollectionByName(col.getName());
        } else if (val instanceof SavedRequest req) {
            Collection parent = getParentCollection(req);
            if (parent != null) {
                store.removeRequestFromCollection(parent.getName(), req);
            }
        }
    }

    private Collection getParentCollection(SavedRequest req) {
        TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
        TreeItem<Object> parent = selected != null ? selected.getParent() : null;
        return (parent != null && parent.getValue() instanceof Collection col) ? col : null;
    }

    /**
     * Displays a simple alert dialog to the user.
     *
     * @param alertType The type of alert (e.g., AlertType.ERROR, AlertType.INFORMATION).
     * @param title     The title of the alert window.
     * @param message   The message to display in the alert dialog.
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Optional: you can set this to display a header
        alert.setContentText(message);
        alert.showAndWait(); // show and wait for the user to close the dialog
    }
}
