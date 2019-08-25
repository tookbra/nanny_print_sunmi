package com.wash.iot.nanny.nanny_print_sunmi;


public class PrinterUtils {

    private static final Integer LINE_LENGTH = 31;

    private static final Integer LINE_WIDTH = 16;

    public static String printInOneLine(String str1, String str2) {
        return printInOneLine(str1, str2, 0);
    }

    public static String printInOneLine(String str1, String str2, int textSize) {
        int needEmpty = LINE_LENGTH - (getStringWidth(str1) + getStringWidth(str2)) % LINE_LENGTH;
        String empty = "";
        while (needEmpty > 0) {
            empty += " ";
            needEmpty--;
        }
        return str1 + empty + str2;
    }

    public static String printLine() {
        int length = LINE_WIDTH;
        String line = "";
        while (length > 0) {
            line += "- ";
            length--;
        }
        return line;
    }

    private static int getStringWidth(String str) {
        int width = 0;
        for (char c : str.toCharArray()) {
            width += isChinese(c) ? 2 : 1;
        }
        return width;
    }

    private static boolean isChinese(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        return ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
                || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION;
    }
}
