package org.kendar.utils;

import java.util.Locale;

public class SimpleStringUtils {
    public static String[] splitByString(String separator,String toSplit){
        var result = new String[2];
        var pos = toSplit.toLowerCase(Locale.ROOT).indexOf(separator.toLowerCase(Locale.ROOT));
        if(pos<0) return result;
        result[0] = toSplit.substring(0,pos);
        result[1] = toSplit.substring(pos+separator.length());
        return result;
    }
}
