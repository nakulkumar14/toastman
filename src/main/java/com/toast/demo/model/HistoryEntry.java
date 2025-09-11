package com.toast.demo.model;

import java.time.Instant;
import java.util.Map;

class HistoryEntry {

    private String method;
    private String url;
    private Map<String, String> headers;
    private String body;
    private Instant timestamp;
}
