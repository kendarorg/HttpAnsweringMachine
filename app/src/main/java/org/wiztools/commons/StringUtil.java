/*
 * Copyright WizTools.org
 * Licensed under the Apache License, Version 2.0:
 * http://www.apache.org/licenses/LICENSE-2.0
 */
package org.wiztools.commons;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author subwiz
 */
public final class StringUtil {

    // Don't allow initialization of this class:
    private StringUtil(){}

    /**
     * Checks if the String is null, or an empty string.
     * @param str The string to check.
     * @return Return value of the validation.
     */
    public static boolean isEmpty(final String str) {
        if (str == null || str.trim().isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the string is not null and not empty.
     * @param str
     * @return true is not empty.
     */
    public static boolean isNotEmpty(final String str) {
        return !isEmpty(str);
    }

    /**
     *
     * @param str The string to verify for null content.
     * @return Returns null string if null is encountered, else the same reference to the string.
     */
    public static String getNullStrIfNull(final String str) {
        return str == null ? "" : str;
    }

    /**
     * A method similar to PHP's implode() function (http://php.net/implode)
     * @param glue The String to glue pieces together.
     * @param pieces An array of String which needs to be glued.
     * @return Returns the concatenated string.
     */
    public static String implode(final String glue, final String[] pieces){
        StringBuilder sb = new StringBuilder();
        final int last = pieces.length;
        int count = 1;
        for(String str: pieces){
            sb.append(str);
            if(count<last && glue != null){
                sb.append(glue);
            }
            count++;
        }
        return sb.toString();
    }

    /**
     * A method similar to PHP's implode() function (http://php.net/implode)
     * @param pieces An array of String which needs to be glued.
     * @return Returns the concatenated string.
     */
    public static String implode(final String[] pieces){
        return implode(null, pieces);
    }

    // An empty String array used for toArray() operation
    public final static String[] STRING_ARRAY = new String[]{};

    /**
     * A method similar to PHP's implode() function (http://php.net/implode)
     * @param glue The String to glue pieces together.
     * @param pieces A collection of String which needs to be glued.
     * @return Returns the concatenated string.
     */
    public static String implode(final String glue, final Collection<String> pieces){
        return implode(glue, pieces.toArray(STRING_ARRAY));
    }

    /**
     * A method similar to PHP's implode() function (http://php.net/implode)
     * @param pieces A collection of String which needs to be glued.
     * @return Returns the concatenated string.
     */
    public static String implode(final Collection<String> pieces){
        return implode(pieces.toArray(STRING_ARRAY));
    }

    /**
     * A method similar to PHP's explode() function (http://php.net/explode)
     * This method does not use RegularExpression based split as in Java. This
     * makes this method much faster than Java's implementation.
     * @param delimiter The delimiter string to split the string with.
     * @param str The input string.
     * @return The List of split string.
     */
    public static List<String> explode(final String delimiter, final String str) {
        final List<String> out = new ArrayList<String>();
        final int len = delimiter.length();

        int startIndex = 0;
        int currIndex = 0;
        while((currIndex = str.indexOf(delimiter, startIndex)) != -1) {
            final String sub = str.substring(startIndex, currIndex);
            out.add(sub);
            startIndex = currIndex + len;
        }
        // Get the tail end of the split string
        final String sub = str.substring(startIndex);
        out.add(sub);

        return out;
    }

    /**
     * When delimiter is found in the string, the string is split into two parts:
     * sub-string till the delimiter, and the string after the delimiter.
     * @param delimiter The delimiter string.
     * @param str The string to operate on.
     * @return List of the result.
     */
    public static List<String> explodeFirst(final String delimiter, final String str) {
        final List<String> out = new ArrayList<String>(2);

        final int beginIndex = str.indexOf(delimiter);
        if(beginIndex != -1) {
            final String subFirst = str.substring(0, beginIndex);
            final String subLast = str.substring(beginIndex + delimiter.length());

            out.add(subFirst);
            out.add(subLast);
        }

        return out;
    }

    /**
     * Splits the string into two parts based on the last occurrence of the delimiter.
     * @param delimiter The delimiter.
     * @param str The string to operate on.
     * @return The List of exploded string.
     */
    public static List<String> explodeLast(final String delimiter, final String str) {
        final List<String> out = new ArrayList<String>(2);

        final int lastIndex = str.lastIndexOf(delimiter);
        if(lastIndex != -1) {
            final String subFirst = str.substring(0, lastIndex);
            final String subLast = str.substring(lastIndex + delimiter.length());

            out.add(subFirst);
            out.add(subLast);
        }

        return out;
    }

    /**
     * Capatilize only the first letter of the string.
     * @param str The input string.
     * @return The capatilized string.
     */
    public static String capatilizeFirstLetter(final String str) {
        final char[] c = str.toCharArray();
        if(c.length > 0) {
            c[0] = Character.toUpperCase(c[0]);
        }
        return String.valueOf(c);
    }

    /**
     * Capatilize first letter of each word after a space, dot (.) or single-quote (').
     * Usually used for converting people names to human-addressable formats.
     * @param str The input string.
     * @return The final capatilized string.
     */
    public static String capatilizeFirstLetterEachWord(final String str) {
        char[] c = str.toLowerCase().toCharArray();
        boolean found = false;
        for(int i=0; i<c.length; i++) {
            if (!found && Character.isLetter(c[i])) {
                c[i] = Character.toUpperCase(c[i]);
                found = true;
            }
            else if(Character.isWhitespace(c[i]) || c[i]=='.' || c[i]=='\'') {
                found = false;
            }
        }
        return String.valueOf(c);
    }

    /**
     * Reverses the capitalization of the string. Eg. 'WizTools.org' becomes 'wIZtOOLS.ORG'.
     * @param str The string which needs to be converted.
     * @return The reverse captitalized string.
     */
    public static String reverseCapitalization(final String str) {
        char[] c = str.toCharArray();
        for(int i=0; i<c.length; i++) {
            if(Character.isLetter(c[i])) {
                c[i] = Character.isLowerCase(c[i])? Character.toUpperCase(c[i]): Character.toLowerCase(c[i]);
            }
        }
        return String.valueOf(c);
    }

    /**
     * Trim text to the last whitespace character within range. It is suggested
     * to pass String.trim()ed text to this method.
     * @param str The input text that needs to be trimmed.
     * @param length The max-length to which it is trimmed to.
     * @return The trimmed text.
     */
    public static String languageTrim(final String str, final int length) {
        if(length < 2) {
            throw new IllegalArgumentException("Length should not be less than 2.");
        }

        // Input string is less-than-or-equal to defined length:
        if(str.length() <= length) {
            return str;
        }

        // Input string is greater than input string:
        final char[] arr = str.toCharArray();
        int lastSpaceCharacter = -1;
        for(int i=length; i>-1; i--) {
            if(Character.isWhitespace(arr[i])) {
                lastSpaceCharacter = i;
                break;
            }
        }

        // when there is no space character, do hard-trim:
        if(lastSpaceCharacter <= 0) {
            return new String(arr, 0, length);
        }
        else {
            return new String(arr, 0, lastSpaceCharacter);
        }
    }
}