package com.toast.demo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fxmisc.richtext.StyleClassedTextArea;

public class HtmlHighlighter {
    public static void highlightHtml(StyleClassedTextArea area, String html) {
        area.clear();

        // Very basic highlighting: tags vs text
        Pattern pattern = Pattern.compile("(<[^>]+>)|([^<]+)");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                area.append(matcher.group(1), "html-tag");
            } else if (matcher.group(2) != null) {
                area.append(matcher.group(2), "html-text");
            }
        }
    }
}
