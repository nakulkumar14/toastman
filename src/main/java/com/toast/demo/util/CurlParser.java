package com.toast.demo.util;

import com.toast.demo.model.SavedRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurlParser {

    private static final Logger log = LoggerFactory.getLogger(CurlParser.class);

    //    private static final Pattern PATTERN = Pattern.compile("(--\\w+|-\\w|'[^']+'|\"[^\"]+\"|\\S+)");
    private static final Pattern PATTERN = Pattern.compile("(--[A-Za-z0-9-]+|-\\w|'[^']+'|\"[^\"]+\"|\\S+)");

    public static SavedRequest parseCurl(String rawCurl) {
//        log.debug("Raw cURL input: {}", rawCurl);
        String normalizedCurl = normalizeCurl(rawCurl);
//        log.info("Normalized cURL: {}", normalizedCurl);

        String method = "GET";
        String url = null;
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, String> formData = new LinkedHashMap<>();
        StringBuilder rawBody = new StringBuilder();

        // Regex to capture flags, quoted strings, and other non-space tokens
        Matcher matcher = PATTERN.matcher(normalizedCurl);
        String lastFlag = null;

        while (matcher.find()) {
            String token = matcher.group(1);

            log.debug("Token: {}, lastFlag={}", token, lastFlag);

            if (token.equalsIgnoreCase("curl")) {
                continue;
            }

            if (token.startsWith("-")) {
                lastFlag = token;
            } else {
                if (lastFlag != null) {
                    method = processToken(lastFlag, token, headers, formData, rawBody, method);
                    lastFlag = null; // Reset the flag after processing its value
                }

                // If the token is a URL, and we haven't found one yet, capture it.
                if (url == null && isUrl(token)) {
                    url = stripQuotes(token);
                }
            }
        }

        String body;
        String bodyType = "Plain Text";
        if (!formData.isEmpty()) {
            body = encodeFormData(formData);
            headers.putIfAbsent("Content-Type", "application/x-www-form-urlencoded");
            method = "POST";
            bodyType = "Form Data";

        } else {
            body = rawBody.toString();
            if (!body.isEmpty()) {
                bodyType = "JSON"; // default guess, can refine later
                if (method.equalsIgnoreCase("GET")) {
                    method = "POST";
                }
            }
        }

        log.info("Parsed request: method={}, url={}, headers={}, body={}",
            method, url, headers, body);

        return new SavedRequest("Imported Request", method, url, headers, body, formData, bodyType);

    }

    /**
     * Helper method to process a token based on the last identified flag.
     */
    private static String processToken(String flag, String value, Map<String, String> headers,
        Map<String, String> formData, StringBuilder rawBody, String currentMethod) {

        log.debug("Processing flag={}, value={}, currentMethod={}", flag, value, currentMethod);

        String strippedValue = stripQuotes(value);

        switch (flag) {
            case "-X":
            case "--request":
                return strippedValue.toUpperCase();

            case "-H":
            case "--header":
                parseHeader(strippedValue, headers);
                break;

            case "-d":
            case "--data":
            case "--data-raw":
                rawBody.append(strippedValue);
                // Having raw body → implies POST
                return "POST";

            case "--form":
            case "--data-urlencode":
                parseFormData(strippedValue, formData);
                // Having form data → implies POST
                return "POST";
        }
        return currentMethod;
    }

    /**
     * Parses a single header string and adds it to the headers map.
     */
    private static void parseHeader(String headerString, Map<String, String> headers) {
        String[] parts = headerString.split(":", 2);
        if (parts.length == 2) {
            headers.put(parts[0].trim(), parts[1].trim());
        }
    }

    private static void parseFormData(String data, Map<String, String> formData) {
        String[] parts = data.split("=", 2);
        if (parts.length == 2) {
            String key = URLDecoder.decode(parts[0].trim(), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(stripQuotes(parts[1].trim()), StandardCharsets.UTF_8);
            formData.put(key, value);
        }
    }

    private static String encodeFormData(Map<String, String> formData) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : formData.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private static String stripQuotes(String s) {
        if (s.length() > 1 && (s.startsWith("'") && s.endsWith("'") || s.startsWith("\"") && s.endsWith("\""))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static String normalizeCurl(String curl) {
        // Handle escaped newlines for readability
        String normalized = curl.replaceAll("\\\\\n", " ").replaceAll("\\\\", " ");
        return normalized.trim();
    }

    private static boolean isUrl(String token) {
        String stripped = stripQuotes(token);
        return stripped.startsWith("http://") || stripped.startsWith("https://");
    }
}
