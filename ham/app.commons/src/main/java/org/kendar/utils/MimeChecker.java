package org.kendar.utils;

import java.util.Locale;

public class MimeChecker {
    private static final String[] STATIC_FILES = {
            ".jpg", ".jpeg", ".ico", ".png", ".gif", ".woff2", ".woff", ".otf", ".ttf", ".eot", ".zip",
            ".pdf", ".tif", ".svg", ".tar", ".gz", ".tgz", ".rar", ".html", ".htm", ".js", ".map", ".jpg",
            ".jpeg", ".css", ".json", ".ts"
    };

    @SuppressWarnings("RedundantIfStatement")
    public static boolean isBinary(String mime, String contentEncoding) {
        if (contentEncoding == null) {
            return true;
        }
        if (mime == null || mime.isEmpty()) {
            return false;
        }
        var mimeLow = mime.toLowerCase(Locale.ROOT);
        if (mimeLow.contains("text")) return false;
        if (mimeLow.contains("xml")) return false;
        if (mimeLow.contains("soap")) return false;
        if (mimeLow.contains("javascript")) return false;
        if (mimeLow.contains("json")) return false;
        if (mimeLow.contains(ConstantsMime.JSON_SMILE)) return false;
        if (mimeLow.contains("application/x-www-form-urlencoded")) return false;
        return true;
    }

    public static boolean isStatic(String mime, String path) {
        if (mime != null) {
            var mimeLow = mime.toLowerCase(Locale.ROOT);
            if (mimeLow.startsWith("text") || mimeLow.startsWith("image")) {
                return true;
            }
        }
        var pathlow = path.toLowerCase(Locale.ROOT);
        if (pathlow.equalsIgnoreCase("/") || pathlow.equalsIgnoreCase("")) {
            return true;
        }
        for (int i = 0; i < STATIC_FILES.length; i++) {
            if (pathlow.endsWith(STATIC_FILES[i])) return true;
        }
        return false;
    }
}
