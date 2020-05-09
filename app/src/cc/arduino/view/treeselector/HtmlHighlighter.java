package cc.arduino.view.treeselector;
import java.awt.*;
import java.util.regex.Pattern;

public class HtmlHighlighter {
    private static final String HighLightTemplate = "<span style='background:rgb(100,230,0);'>$1</span>";

    public static String highlightText(String text, String textToHighlight) {
        if (textToHighlight.length() == 0) {
            return text;
        }

        try {
            text = text.replaceAll("(?i)(" + Pattern.quote(textToHighlight) + ")", HighLightTemplate);
        } catch (Exception e) {
            return text;
        }
        return text;
    }
}