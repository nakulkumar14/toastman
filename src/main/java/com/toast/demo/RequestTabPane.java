package com.toast.demo;

import com.toast.demo.components.RequestTab;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class RequestTabPane extends TabPane {

    private final Tab addTab = createAddTab();
    private int tabCounter = 1;

    public RequestTabPane() {
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        initializeTabs();

        // Now configure the "+" tab behavior
        addTab.setOnSelectionChanged(e -> {
            if (addTab.isSelected()) {
                addRequestTab();
            }
        });
    }

    private void initializeTabs() {
        // Create and add initial request tab
        RequestTab firstTab = createRequestTab("Tab " + tabCounter++);
        getTabs().add(firstTab);

        // Add "+" tab
        getTabs().add(addTab);
        getSelectionModel().select(firstTab);
    }

    private Tab createAddTab() {
        Tab tab = new Tab("+");
        tab.setClosable(false);

        tab.setOnSelectionChanged(e -> {
            if (tab.isSelected()) {
                addRequestTab();
            }
        });

        return tab;
    }

    private void addRequestTab() {
        RequestTab newTab = createRequestTab("Tab " + tabCounter++);
        this.getTabs().add(getTabs().size() - 1, newTab); // Add before "+" tab
        this.getSelectionModel().select(newTab);    // Auto-select the new tab
    }

    private RequestTab createRequestTab(String title) {
        return new RequestTab(title);
    }
}
