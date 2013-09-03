package org.veloscope.utils;

public class Strings {
    public static boolean empty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean notEmpty(String s) {
        return !empty(s);
    }

    public static String capitalize(String s) {
        if (empty(s)) {
            return s;
        }

        return (s.substring(0, 1).toUpperCase() + s.substring(1, s.length()));

    }
}
