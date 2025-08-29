package com.toast.demo.util;

import java.util.Map;

public class CurlGenerator {

    public static String generateCurl(String method, String url, Map<String, String> headers, String body) {
        StringBuilder curl = new StringBuilder("curl -X " + method.toUpperCase());

        // Append headers
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            curl.append(" -H ")
                .append("\"")
                .append(entry.getKey())
                .append(": ")
                .append(entry.getValue())
                .append("\"");
        }

        // Append body (for POST/PUT)
        if (body != null && !body.isBlank() && (method.equalsIgnoreCase("POST") || method.equalsIgnoreCase("PUT"))) {
            // Escape double quotes inside body
            String escapedBody = body.replace("\"", "\\\"");
            curl.append(" -d ")
                .append("\"")
                .append(escapedBody)
                .append("\"");
        }

        // Append URL
        curl.append(" \"").append(url).append("\"");

        return curl.toString();
    }

}
