package zim.tave.memory.util;

import java.util.regex.Pattern;

public class EmojiValidator {

    private EmojiValidator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 문자열이 유효한 이모지인지 확인 (복합 이모지 포함, 국기 이모지 등)
     */
    public static boolean isValidEmoji(String emoji) {
        if (emoji == null || emoji.trim().isEmpty()) {
            return false;
        }
        int codePointCount = emoji.codePointCount(0, emoji.length());
        if (codePointCount == 1) {
            int cp = emoji.codePointAt(0);
            // 대표적 이모지 범위
            return (cp >= 0x1F600 && cp <= 0x1F64F) // Emoticons
                || (cp >= 0x1F300 && cp <= 0x1F5FF) // Misc Symbols and Pictographs
                || (cp >= 0x1F680 && cp <= 0x1F6FF) // Transport and Map
                || (cp >= 0x2600 && cp <= 0x26FF)   // Misc Symbols
                || (cp >= 0x2700 && cp <= 0x27BF);  // Dingbats
        } else if (codePointCount == 2) {
            int cp1 = emoji.codePointAt(0);
            int cp2 = emoji.codePointAt(emoji.offsetByCodePoints(0, 1));
            // 국기 이모지: 두 코드포인트 모두 Regional Indicator Symbol
            return (cp1 >= 0x1F1E6 && cp1 <= 0x1F1FF) && (cp2 >= 0x1F1E6 && cp2 <= 0x1F1FF);
        }
        return false;
    }

    /**
     * 이모지의 바이트 길이 확인 (utf8mb4에서 4바이트)
     */
    public static int getEmojiByteLength(String emoji) {
        if (emoji == null) {
            return 0;
        }
        return emoji.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
    }

    /**
     * 이모지가 utf8mb4에서 올바르게 저장될 수 있는지 확인
     */
    public static boolean isUtf8mb4Compatible(String emoji) {
        if (!isValidEmoji(emoji)) {
            return false;
        }
        int byteLength = getEmojiByteLength(emoji);
        return byteLength <= 8; // utf8mb4에서 이모지는 최대 8바이트 (국가 플래그 등)
    }
}
