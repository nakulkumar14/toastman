package com.toast.demo.util;

public class StatusUtils {

    /**
     * Returns a CSS color string for the given HTTP status code.
     */
    public static String getColorForStatusCode(int statusCode) {
        return switch (statusCode / 100) {
            case 2 -> "green";
            case 3 -> "blue";
            case 4 -> "orange";
            case 5 -> "red";
            default -> "black";
        };
    }

    /**
     * Returns a readable label like "Success", "Client Error", etc.
     */
    public static String getLabelForStatusCode(int statusCode) {
        return switch (statusCode / 100) {
            case 2 -> "Success";
            case 3 -> "Redirection";
            case 4 -> "Client Error";
            case 5 -> "Server Error";
            default -> "Info";
        };
    }
}