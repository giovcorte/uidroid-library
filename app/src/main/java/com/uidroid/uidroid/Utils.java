package com.uidroid.uidroid;

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

}
