package com.toast.demo.util;

import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonHighlighter {

    private static final Pattern STRING_PATTERN = Pattern.compile("\"(.*?)\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("\\b(true|false|null)\\b");
    private static final Pattern JSON_PATTERN = Pattern.compile(
        "(?<KEY>\"(\\\\.|[^\"])*\"\\s*:)"   // keys ending with colon
            + "|(?<STRING>\"(\\\\.|[^\"])*\")"   // string values
            + "|(?<NUMBER>-?\\d+(\\.\\d+)?)"     // numbers
            + "|(?<BOOLEAN>true|false)"          // booleans
            + "|(?<NULL>null)"                   // null
    );

    public static void highlightJson(StyleClassedTextArea area, String json) {
        area.clear();
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        Matcher matcher = JSON_PATTERN.matcher(json);
        int last = 0;

        while (matcher.find()) {
            // Append unstyled text before the match
            if (matcher.start() > last) {
                area.appendText(json.substring(last, matcher.start()));
            }

            String styleClass =
                matcher.group("KEY") != null ? "json-key" :
                    matcher.group("STRING") != null ? "json-string" :
                        matcher.group("NUMBER") != null ? "json-number" :
                            matcher.group("BOOLEAN") != null ? "json-boolean" :
                                matcher.group("NULL") != null ? "json-null" :
                                    null;

            area.append(matcher.group(), styleClass);
            last = matcher.end();
        }

        // Append remaining text
        if (last < json.length()) {
            area.appendText(json.substring(last));
        }
    }

    private static void applyPattern(StyleClassedTextArea area, Pattern pattern, String styleClass) {
        Matcher matcher = pattern.matcher(area.getText());
        while (matcher.find()) {
            area.setStyleClass(matcher.start(), matcher.end(), styleClass);
        }
    }
}
