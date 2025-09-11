package com.toast.demo;

import com.toast.demo.components.CollectionsView;
import com.toast.demo.components.HistoryView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class HttpRequestUI extends Application {

    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        setupPrimaryStage(primaryStage);
    }

    private void setupPrimaryStage(Stage stage) {
        BorderPane root = new BorderPane();

        // Main content area (default: tabbed request editor)
        RequestTabPane tabPane = new RequestTabPane();
        root.setCenter(tabPane);

        // Left sidebar
        VBox sidebar = createSidebar(root, tabPane);
//        root.setLeft(sidebar);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        stage.setTitle("Multi‑Tab HTTP Client");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createSidebar(BorderPane root, RequestTabPane tabPane) {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(10));
        sidebar.setPrefWidth(60);
        sidebar.getStyleClass().add("sidebar");

        // Container for sidebar + collections view
        HBox leftContainer = new HBox(sidebar);
        root.setLeft(leftContainer);

        // Collections view (hidden by default)
        CollectionsView collectionsView = new CollectionsView(tabPane);
        collectionsView.setVisible(false);
        collectionsView.setManaged(false); // ensures layout doesn’t reserve space

        HistoryView historyView = new HistoryView(tabPane);
        historyView.setVisible(false);
        historyView.setManaged(false);

        // Collections button
        Button collectionsBtn = new Button("Collections"); // icon placeholder
        collectionsBtn.getStyleClass().add("sidebar-button");
        collectionsBtn.setOnAction(e -> {
            boolean showing = collectionsView.isVisible();
            if (showing) {
                collectionsView.setVisible(false);
                collectionsView.setManaged(false);
            } else {
                showSidebarView(collectionsView, leftContainer, collectionsView, historyView);
            }
        });

        Button historyBtn = new Button("History"); // icon placeholder
        historyBtn.getStyleClass().add("sidebar-button");
        historyBtn.setOnAction(e -> {
            boolean alreadyVisible = historyView.isVisible();
            if (alreadyVisible) {
                historyView.setVisible(false);
                historyView.setManaged(false);
            } else {
                showSidebarView(historyView, leftContainer, collectionsView, historyView);
                historyView.refresh(); // reload items
            }
        });

        sidebar.getChildren().addAll(collectionsBtn, historyBtn);

        return sidebar;
    }

    private void showSidebarView(Node viewToShow, HBox leftContainer, Node... allViews) {
        for (Node view : allViews) {
            if (view == viewToShow) {
                if (!leftContainer.getChildren().contains(view)) {
                    leftContainer.getChildren().add(view);
                }
                view.setVisible(true);
                view.setManaged(true);
            } else {
                view.setVisible(false);
                view.setManaged(false);
            }
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}
