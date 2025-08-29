package com.toast.demo.model;

import java.net.http.HttpHeaders;
import java.util.Map;

public class HttpResponseDTO {

    private int statusCode;
    private String body;
    private Map<String, String> headers;

    public HttpResponseDTO(int statusCode, String body, HttpHeaders headers) {
        this.statusCode = statusCode;
        this.body = body;
        this.headers = headers.map().entrySet().stream()
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, e -> String.join(", ", e.getValue())));
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
}
