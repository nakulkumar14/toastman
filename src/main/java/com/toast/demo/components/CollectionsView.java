package com.toast.demo.components;

import com.toast.demo.RequestTabPane;
import com.toast.demo.model.Collection;
import com.toast.demo.model.Folder;
import com.toast.demo.model.SavedRequest;
import com.toast.demo.service.CollectionsStore;
import com.toast.demo.util.CurlParser;
import java.io.File;
import java.io.IOException;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
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
import javafx.scene.layout.Priority;
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
    private final Button addFolderButton = new Button("Add Folder");

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
        VBox topBox = new VBox(5, toolBar, buildSearchBox(), addFolderButton);
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

            private final Button menuButton = new Button("⋮");
            private final HBox cellBox = new HBox(5);
            private final Label nameLabel = new Label();

            {
                menuButton.setVisible(false);
                menuButton.setFocusTraversable(false);
                menuButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                // Context menu actions
                MenuItem exportItem = new MenuItem("Export");
                exportItem.setOnAction(e -> {
                    Object val = getItem();
                    if (val instanceof Collection col) {
                        exportCollection(col);
                    }
                });

                MenuItem renameItem = new MenuItem("Rename");
                renameItem.setOnAction(e -> {
                    Object val = getItem();
                    if (val instanceof Collection col) {
                        renameCollection(col);
                    }
                });

                MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction(e -> {
                    Object val = getItem();
                    if (val instanceof Collection col) {
                        store.removeCollectionByName(col.getName());
                    }
                });

                ContextMenu contextMenu = new ContextMenu(exportItem, renameItem, deleteItem);
                menuButton.setOnAction(e -> contextMenu.show(menuButton, Side.RIGHT, 0, 0));

                // Layout
                HBox.setHgrow(nameLabel, Priority.ALWAYS);
                cellBox.setAlignment(Pos.CENTER_LEFT);
                cellBox.getChildren().addAll(nameLabel, menuButton);
            }

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(""); // reset style
                    return;
                } else if (item instanceof Collection c) {
                    setText(null);
                    nameLabel.setText(c.getName());
                    setGraphic(cellBox);

                    // Show 3-dots only on hover
                    setOnMouseEntered(e -> menuButton.setVisible(true));
                    setOnMouseExited(e -> menuButton.setVisible(false));
                } else if (item instanceof Folder f) {
                    setGraphic(null);
                    setText("📂 " + f.getName());
                    setStyle("");
                } else if (item instanceof SavedRequest r) {
                    setGraphic(null);
                    String method = r.getMethod().toUpperCase();
                    setText(method + "  " + r.getName());
                    // Apply colors by method
                    switch (method) {
                        case "GET" -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        case "POST" -> setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                        case "PUT" -> setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                        case "DELETE" -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                        default -> setStyle("-fx-text-fill: gray; -fx-font-weight: bold;");
                    }
                } else {
                    setGraphic(null);
                    setText(item.toString());
                    setStyle("");
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

    private void exportCollection(Collection col) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Collection");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Files", "*.json"));
        fileChooser.setInitialFileName(col.getName().replaceAll("\\s+", "_") + ".json");

        File file = fileChooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                CollectionsStore.getInstance().exportCollection(col, file);
                showAlert(Alert.AlertType.INFORMATION, "Export Successful",
                    "Collection exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                log.error("Export failed", e);
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to export collection: " + e.getMessage());
            }
        }
    }

    private void renameCollection(Collection col) {
        TextInputDialog dialog = new TextInputDialog(col.getName());
        dialog.setHeaderText("Rename collection:");
        dialog.setTitle("Rename Collection");
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isBlank()) {
                col.setName(newName);
                store.updateCollection(col);
                refresh();
            }
        });
    }

    private void setupButtonActions() {
        // add collection
        addButtonAction();
        // delete collection
        deleteButtonAction();
        importButtonAction();

        addFolderButtonAction();
    }

    private void addFolderButtonAction() {
        addFolderButton.setOnAction(e -> {
            Object val = getSelectedValue();
            if (val instanceof Collection col) {
                TextInputDialog dialog = new TextInputDialog("New Folder");
                dialog.setHeaderText("Enter folder name:");
                dialog.showAndWait().ifPresent(name -> {
                    col.addFolder(new Folder(name));
                    store.updateCollection(col); // overwrite persisted
                    refresh();
                });
            } else if (val instanceof Folder folder) {
                TextInputDialog dialog = new TextInputDialog("New SubFolder");
                dialog.setHeaderText("Enter folder name:");
                dialog.showAndWait().ifPresent(name -> {
                    folder.addSubFolder(new Folder(name));
                    Collection parentCollection = getParentCollection(folder);
                    if (parentCollection != null) {
                        store.updateCollection(parentCollection); // ensure persistence
                    }
                    refresh();
                });
            }
        });
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
        treeView.setShowRoot(false);
    }

    private TreeItem<Object> buildTree(String query) {
        TreeItem<Object> root = new TreeItem<>("Collections");
        root.setExpanded(true);
        String q = query == null ? null : query.toLowerCase();

        for (Collection col : store.getCollections()) {
            TreeItem<Object> colItem = new TreeItem<>(col);

            // Add requests at root level
            for (SavedRequest req : col.getRequests()) {
                if (matchesQuery(req, col, q)) {
                    colItem.getChildren().add(new TreeItem<>(req));
                }
            }
            // Add folders recursively
            for (Folder folder : col.getFolders()) {
                TreeItem<Object> folderItem = buildFolderTree(folder, col, q);
                colItem.getChildren().add(folderItem);
            }

            root.getChildren().add(colItem);
        }
        return root;
    }

    private TreeItem<Object> buildFolderTree(Folder folder, Collection col, String q) {
        TreeItem<Object> folderItem = new TreeItem<>(folder);

        for (SavedRequest req : folder.getRequests()) {
            if (matchesQuery(req, col, q)) {
                folderItem.getChildren().add(new TreeItem<>(req));
            }
        }

        for (Folder sub : folder.getSubFolders()) {
            TreeItem<Object> subItem = buildFolderTree(sub, col, q);
            folderItem.getChildren().add(subItem);
        }

        return folderItem;
    }

    private boolean matchesQuery(SavedRequest req, Collection col, String q) {
        return q == null ||
            req.getName().toLowerCase().contains(q) ||
            req.getUrl().toLowerCase().contains(q) ||
            col.getName().toLowerCase().contains(q);
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
        } else if (val instanceof Folder folder) {
            Collection parentCollection = getParentCollection(folder);
            if (parentCollection != null) {
                parentCollection.getFolders().removeIf(f -> f.getName().equals(folder.getName()));
                store.updateCollection(parentCollection);
                refresh();
            }
        }
    }

    private Collection getParentCollection(SavedRequest req) {
        TreeItem<Object> selected = treeView.getSelectionModel().getSelectedItem();
        TreeItem<Object> parent = selected != null ? selected.getParent() : null;
        return (parent != null && parent.getValue() instanceof Collection col) ? col : null;
    }

    private Collection getParentCollection(Folder folder) {
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
