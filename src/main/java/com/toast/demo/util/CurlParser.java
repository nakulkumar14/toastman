package com.toast.demo.util;

import com.toast.demo.model.SavedRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class CurlParser {

    private static final Pattern PATTERN = Pattern.compile("(--\\w+|-\\w|'[^']+'|\"[^\"]+\"|\\S+)");

    public static SavedRequest parseCurl(String rawCurl) {
        String normalizedCurl = normalizeCurl(rawCurl);

        String method = "GET";
        String url = null;
        Map<String, String> headers = new LinkedHashMap<>();
        StringBuilder body = new StringBuilder();

        // Regex to capture flags, quoted strings, and other non-space tokens
        Matcher matcher = PATTERN.matcher(normalizedCurl);
        String lastFlag = null;

        while (matcher.find()) {
            String token = matcher.group(1);

            if (token.equalsIgnoreCase("curl")) {
                continue;
            }

            if (token.startsWith("-")) {
                lastFlag = token;
            } else {
                if (lastFlag != null) {
                    processToken(lastFlag, token, headers, body);
                    lastFlag = null; // Reset the flag after processing its value
                }

                // If the token is a URL, and we haven't found one yet, capture it.
                if (url == null && isUrl(token)) {
                    url = stripQuotes(token);
                }
            }
        }

        // Apply a POST method if a body exists
        if (!body.toString().isEmpty() && method.equalsIgnoreCase("GET")) {
            method = "POST";
        }

        return new SavedRequest("Imported Request", method, url, headers, body.toString());
    }

    /**
     * Helper method to process a token based on the last identified flag.
     */
    private static void processToken(String flag, String value, Map<String, String> headers, StringBuilder body) {
        String strippedValue = stripQuotes(value);
        if (flag.equalsIgnoreCase("-X") || flag.equalsIgnoreCase("--request")) {
            // Method is handled by the main loop
        } else if (flag.equalsIgnoreCase("-H") || flag.equalsIgnoreCase("--header")) {
            parseHeader(strippedValue, headers);
        } else if (flag.equalsIgnoreCase("-d") || flag.equalsIgnoreCase("--data") || flag.equalsIgnoreCase("--data-raw")) {
            body.append(strippedValue);
        } else if (flag.equals("--form")) {
            body.append(strippedValue).append("&");
        }
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
