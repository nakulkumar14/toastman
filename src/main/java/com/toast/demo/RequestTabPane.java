package com.toast.demo;

import com.toast.demo.components.RequestTab;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class RequestTabPane extends TabPane {

    private final Tab addTab = new Tab("+");

    public RequestTabPane() {
        setTabClosingPolicy(TabClosingPolicy.ALL_TABS);

        // Initialize + tab
        addTab.setClosable(false);
        // Add the "+" tab first
        this.getTabs().add(addTab);

        // Add one default request tab at index 0
        RequestTab defaultTab = new RequestTab("Tab 1");
        this.getTabs().add(0, defaultTab);
        this.getSelectionModel().select(defaultTab); // Select it by default

        // Now configure the "+" tab behavior
        addTab.setOnSelectionChanged(e -> {
            if (addTab.isSelected()) {
                addRequestTab();
            }
        });
    }

    private void addRequestTab() {
        int index = getTabs().size(); // +1 for "Add" tab
        RequestTab newTab = new RequestTab("Tab " + index);
        this.getTabs().add(getTabs().size() - 1, newTab);
        this.getSelectionModel().select(newTab);
    }
}
