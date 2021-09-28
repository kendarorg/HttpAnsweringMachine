package org.kendar.utils;

import java.util.Locale;

public class MimeChecker {
    public static boolean isBinary(String mime,String contentEncoding){
        if(contentEncoding!=null){
            return true;
        }
        var mimeLow = mime.toLowerCase(Locale.ROOT);
        if(mimeLow.startsWith("text")) return false;
        if(mimeLow.endsWith("javascript")) return false;
        if(mimeLow.endsWith("json")) return false;
        if(mimeLow.contains("application/x-www-form-urlencoded")) return false;
        return true;
    }

    private static final String[] STATIC_FILES ={
            ".jpg",".jpeg",".ico",".png",".gif",
            ".woff2",".woff",".otf",".ttf",".eot",
            ".zip",".pdf",".tif",".svg",".tar",".gz",".tgz",".rar",
            ".html",".htm",".js",".map",".jpg",".jpeg",".css",".json",".ts"
    };

    public static boolean isStatic(String mime, String path) {
        var mimeLow = mime.toLowerCase(Locale.ROOT);
        var pathlow = path.toLowerCase(Locale.ROOT);
        for (int i=0;i<STATIC_FILES.length;i++) {
            if(pathlow.endsWith(STATIC_FILES[i]))return true;
        }
        return false;
    }
}
