package com.toast.demo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public final class JsonFormatter {

    private static final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    // Prevent instantiation
    private JsonFormatter() {
    }

    public static String prettyPrint(String json) {
        try {
            Object jsonObject = mapper.readValue(json, Object.class);
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
        } catch (Exception e) {
            // Not a JSON string; return as is
            return json;
        }
    }

    public static boolean isValidJson(String json) {
        try {
            mapper.readTree(json);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
