package com.streamcollector.app.util;

import java.util.Locale;

public class StringUtils {
    private static final int CHECK_SQL_INJECTION_MULTIPLIER = 4;
    public static final int CHECK_SQL_INJECTION_MAX_SCORE = 25 * CHECK_SQL_INJECTION_MULTIPLIER;

    public static String formatUserQuery(String text){
        return text.replace("_", "\\_");
    }

    public static int checkSQLInjection(String text){
        text = text.trim().toLowerCase(Locale.ROOT);
        String textWithoutSpaces = text.replace(" ", "");
        int score = 0;

        if (text.contains("||"))
            score += 1;

        if (text.contains(" or") || text.contains("or "))
            score += 1;

        if (text.contains("&&"))
            score += 1;

        if (text.contains(" and") || text.contains("and "))
            score += 1;

        if (text.contains("1=1"))
            score += 1;

        if (text.contains("true=true"))
            score += 1;

        if (text.contains("true=1"))
            score += 1;

        if (text.contains("1=true"))
            score += 1;

        if (text.contains("')"))
            score += 1;

        if (text.contains("' "))
            score += 1;

        if (text.contains("(") && text.contains(")"))
            score += 1;

        if (text.contains("/*"))
            score += 1;

        if (text.contains("*/"))
            score += 1;

        if (text.contains("--"))
            score += 1;

        if (text.contains("#"))
            score += 1;

        if (text.contains("version()"))
            score += 1;

        if (text.contains(" union") || text.contains("union "))
            score += 1;

        if (text.contains("group by") || text.contains("order by"))
            score += 1;

        if (text.contains("group_concat("))
            score += 1;

        if (textWithoutSpaces.contains(";insert") || text.contains(" insert") || text.contains("insert "))
            score += 1;

        if (textWithoutSpaces.contains(";drop") || text.contains(" drop") || text.contains("drop "))
            score += 1;

        if (textWithoutSpaces.contains(";delete") || text.contains(" delete") || text.contains("delete "))
            score += 1;

        if (textWithoutSpaces.contains(";update") || text.contains(" update") || text.contains("update "))
            score += 1;

        if (textWithoutSpaces.contains(";select") || text.contains(" select") || text.contains("select "))
            score += 1;

        if (textWithoutSpaces.contains(";create") || text.contains(" create") || text.contains("create "))
            score += 1;

        return score * CHECK_SQL_INJECTION_MULTIPLIER;
    }

    public static String fixedWidth(String text, char fill, int width){
        if (text.length() < width){
            StringBuilder builder = new StringBuilder(text);
            int addSpaces = width - text.length();
            builder.append(String.valueOf(fill).repeat(addSpaces));
            return builder.toString();
        }
        return text;
    }
}
