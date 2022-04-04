package com.uidroid.uidroid;

import android.util.Patterns;

import java.io.File;

/**
 * Utilities class.
 */
public final class Utils {

    /**
     * Returns true if str is a integer, false otherwise. It's optimized for performance.
     *
     * @param str String to test.
     * @return boolean true if str is integer, false otherwise.
     */
    public static boolean isInteger(String str) {
        if (str == null) {
            return false;
        }

        int length = str.length();

        if (length == 0) {
            return false;
        }

        int i = 0;

        if (str.charAt(0) == '-') {
            if (length == 1) {
                return false;
            }

            i = 1;
        }

        for (; i < length; i++) {
            char c = str.charAt(i);

            if (c < '0' || c > '9') {
                return false;
            }
        }

        return true;
    }

    /**
     * Determines if a string conforms to a url pattern.
     *
     * @param source String maybe url.
     * @return boolean true if String represents a url, false otherwise.
     */
    public static boolean isUrl(String source) {
        return Patterns.WEB_URL.matcher(source).matches();
    }

    /**
     * Determines if the source is a valid File path.
     *
     * @param source String maybe file path.
     * @return boolean true if String represents a valid file, false otherwise.
     */
    public static boolean isFile(String source) {
        final File file = new File(source);

        return file.exists();
    }

    /**
     * Determines if the source can be a drawable resource.
     *
     * @param source String meybe resource
     * @return True if the source is numeral, false otherwise.
     */
    public static boolean isResource(String source) {
        return isInteger(source);
    }

}
