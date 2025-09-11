package com.toast.demo.service;

import com.toast.demo.model.SavedRequest;
import java.util.LinkedList;
import java.util.List;

public class RequestHistory {

    private static final RequestHistory INSTANCE = new RequestHistory();

    private static final int MAX_HISTORY = 20;
    private final LinkedList<SavedRequest> history = new LinkedList<>();

    public static RequestHistory getInstance() {
        return INSTANCE;
    }

    public void add(SavedRequest req) {
        // newest at top
        history.addFirst(req);
        if (history.size() > MAX_HISTORY) {
            history.removeLast();
        }
    }

    public List<SavedRequest> getAll() {
        return List.copyOf(history);
    }
}
