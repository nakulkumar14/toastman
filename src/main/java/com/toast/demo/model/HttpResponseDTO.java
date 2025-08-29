package com.toast.demo.model;

import java.net.http.HttpHeaders;
import java.util.HashMap;
import java.util.Map;

public class HttpResponseDTO {

    private final int statusCode;
    private final String body;
    private final Map<String, String> headers;

    public HttpResponseDTO(int statusCode, String body, HttpHeaders headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = new HashMap<>();
        headers.map().forEach((key, values) -> this.headers.put(key.toLowerCase(), String.join(", ", values)));
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public String getContentType() {
        return headers.getOrDefault("content-type", "");
    }
}
