package com.toast.demo.model;

public class HttpResponseDTO {

    private int statusCode;
    private String body;

    public HttpResponseDTO(int statusCode, String body) {
        this.statusCode = statusCode;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getBody() {
        return body;
    }
}
