package com.toast.demo.util;

import org.fxmisc.richtext.StyleClassedTextArea;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonHighlighter {

    private static final Pattern STRING_PATTERN = Pattern.compile("\"(.*?)\"");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\b\\d+(\\.\\d+)?\\b");
    private static final Pattern BOOLEAN_PATTERN = Pattern.compile("\\b(true|false|null)\\b");

    public static void highlightJson(StyleClassedTextArea area, String json) {
        area.clear();
        area.setStyle("-fx-font-family: monospace; -fx-font-size: 13px;");

        Matcher matcher = STRING_PATTERN.matcher(json);
        int last = 0;

        while (matcher.find()) {
            if (matcher.start() > last) {
                String plain = json.substring(last, matcher.start());
                area.appendText(plain);
            }

            String group = matcher.group();
            area.append(group, "json-string");

            last = matcher.end();
        }

        if (last < json.length()) {
            area.appendText(json.substring(last));
        }

        // Apply other regexes (optional, for full highlighting, you'd tokenize properly)
        applyPattern(area, NUMBER_PATTERN, "json-number");
        applyPattern(area, BOOLEAN_PATTERN, "json-boolean");
    }

    private static void applyPattern(StyleClassedTextArea area, Pattern pattern, String styleClass) {
        Matcher matcher = pattern.matcher(area.getText());
        while (matcher.find()) {
            area.setStyleClass(matcher.start(), matcher.end(), styleClass);
        }
    }
}
