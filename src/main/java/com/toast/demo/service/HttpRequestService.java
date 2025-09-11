package com.toast.demo.service;

import com.toast.demo.model.HttpResponseDTO;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpRequestService {

    public CompletableFuture<HttpResponseDTO> sendRequest(String method, String url, Map<String, String> headers,
        String body) {
        try {
            String normalizedUrl = normalizeUrl(url);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(normalizedUrl));

            headers.forEach(builder::header);

            switch (method.toUpperCase()) {
                case "POST":
                    builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
                    break;
                case "PUT":
                    builder.PUT(HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
                    break;
                case "PATCH":
                    builder.method("PATCH", HttpRequest.BodyPublishers.ofString(body == null ? "" : body));
                    break;
                case "DELETE":
                    builder.DELETE();
                    break;
                default:
                    builder.GET();
                    break;
            }

            HttpRequest request = builder.build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> new HttpResponseDTO(response.statusCode(), response.body(), response.headers()));

        } catch (Exception e) {
            CompletableFuture<HttpResponseDTO> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(e);
            return failedFuture;
        }
    }

    public String normalizeUrl(String url) {
        if (!url.matches("^\\w+://.*")) {
            // If the URL does not start with a protocol, prepend https://
            return "https://" + url;
        }
        return url;
    }


}
