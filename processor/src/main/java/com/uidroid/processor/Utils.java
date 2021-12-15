package com.uidroid.processor;

import java.util.List;

public final class Utils {

    public static String capitalize(String name) {
        if (name.length() == 1) {
            return name.toUpperCase();
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String firstLowerCased(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String getCodeString(String s) {
        if (s == null) {
            return "null";
        }

        return "\"" + s + "\"";
    }

    public static String getClearClassName(String className) {
        if (className.contains("<") && className.contains(">")) {
            final int index = className.indexOf("<");
            return className.substring(0, index);
        } else {
            return className;
        }
    }

    public static String getParams(List<String> strings) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            String param = strings.get(i);
            params.append(firstLowerCased(getSimpleName(param)));
            if (i <= strings.size() - 2) {
                params.append(", ");
            }
        }
        return params.toString();
    }

    public static String getTypedParams(List<String> constructorParameters) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < constructorParameters.size(); i++) {
            String param = constructorParameters.get(i);
            params.append(getSimpleName(param));
            params.append(" ");
            params.append(firstLowerCased(getSimpleName(param)));
            if (i <= constructorParameters.size() - 2) {
                params.append(", ");
            }
        }
        return params.toString();
    }

    public static String getSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return getClearClassName(className.substring(lastDot + 1));
    }

    public static String getCodeParams(String... params) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            result.append(params[i]);
            if (i < params.length - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    public static String getLayoutIdFromClassName(String className) {
        String[] words = className.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])");
        StringBuilder start = new StringBuilder(words[0].toLowerCase());

        for (int i = 1; i < words.length; i++) {
            start.append("_").append(words[i].toLowerCase());
        }

        return start.toString();
    }

}
