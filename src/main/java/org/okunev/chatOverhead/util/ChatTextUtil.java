package org.okunev.chatOverhead.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public final class ChatTextUtil {
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();

    private ChatTextUtil() {
    }

    public static String plain(Component component) {
        return PLAIN.serialize(component);
    }

    /**
     * Calculates the number of visible lines using the same configured width
     * that is assigned to TextDisplay. The wrapping is word-aware and explicit
     * newlines are always respected.
     *
     * This gives the animation a deterministic line count instead of guessing
     * from the raw character count alone.
     */
    public static int countLines(String text, int maxWidthPixels) {
        if (text == null || text.isEmpty()) {
            return 1;
        }

        int lines = 0;
        for (String paragraph : text.replace("\r", "").split("\\n", -1)) {
            lines += countParagraphLines(paragraph, maxWidthPixels);
        }
        return Math.max(1, lines);
    }

    private static int countParagraphLines(String paragraph, int maxWidthPixels) {
        if (paragraph.isEmpty()) {
            return 1;
        }

        int lines = 1;
        int currentWidth = 0;

        for (String word : paragraph.split(" ")) {
            int wordWidth = visualWidth(word);
            int spaceWidth = currentWidth == 0 ? 0 : 4;

            if (currentWidth > 0 && currentWidth + spaceWidth + wordWidth <= maxWidthPixels) {
                currentWidth += spaceWidth + wordWidth;
                continue;
            }

            if (currentWidth > 0) {
                lines++;
            }

            if (wordWidth <= maxWidthPixels) {
                currentWidth = wordWidth;
                continue;
            }

            int remaining = wordWidth;
            while (remaining > maxWidthPixels) {
                lines++;
                remaining -= maxWidthPixels;
            }
            currentWidth = Math.max(1, remaining);
        }

        return lines;
    }

    private static int visualWidth(String value) {
        int width = 0;
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            width += glyphWidth(codePoint);
            i += Character.charCount(codePoint);
        }
        return width;
    }

    private static int glyphWidth(int codePoint) {
        char c = (char) codePoint;
        if (c == ' ') return 4;
        if ("ilI!|.,:;'`".indexOf(c) >= 0) return 2;
        if ("MW@%#".indexOf(c) >= 0) return 7;
        if (c >= 0x2E80) return 8;
        return 6;
    }
}
