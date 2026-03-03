package org.example.expert.util;

import java.util.List;

public class SqlUtil {
    final static private List<String> likeEscapeChars = List.of(
            "%", "_", "[", "]");

    public static String escapeStringForLike(String str, String escapeChar) {
        str = str.replace(escapeChar, escapeChar + escapeChar);

        for (final String s : likeEscapeChars) {
            str = str.replace(s, escapeChar + s);
        }

        return str;
    }
}
