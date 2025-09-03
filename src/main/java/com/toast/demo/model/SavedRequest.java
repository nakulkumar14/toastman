package com.toast.demo.model;

import java.util.Map;
import java.util.UUID;

public class SavedRequest {

    private final String id;
    private String name;
    private String method;
    private String url;
    private Map<String, String> headers;
    private String body;

    public SavedRequest() {
        this.id = UUID.randomUUID().toString();
    } // needed for Jackson

    public SavedRequest(String name, String method, String url, Map<String, String> headers, String body) {
        this.id = UUID.randomUUID().toString(); // generate unique ID
        this.name = name;
        this.method = method;
        this.url = url;
        this.headers = headers;
        this.body = body;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}