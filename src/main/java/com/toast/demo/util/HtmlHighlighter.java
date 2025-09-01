package com.toast.demo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fxmisc.richtext.StyleClassedTextArea;

public class HtmlHighlighter {

    // Compile regex once, not on every call
    private static final Pattern HTML_PATTERN = Pattern.compile("(<[^>]+>)|([^<]+)");

    private HtmlHighlighter() {
        // Utility class: prevent instantiation
    }

    public static void highlightHtml(StyleClassedTextArea area, String html) {
        if (area == null || html == null) {
            return;
        }

        area.clear();

        // Very basic highlighting: tags vs text
        Matcher matcher = HTML_PATTERN.matcher(html);

        while (matcher.find()) {
            String tag = matcher.group(1);
            String text = matcher.group(2);

            if (tag != null) {
                area.append(tag, "html-tag");
            } else if (text != null && !text.isEmpty()) {
                area.append(text, "html-text");
            }
        }
    }
}
