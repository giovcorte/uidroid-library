package com.uidroid.processor;

import java.util.List;

/**
 * Utility methods
 */
public final class Utils {

    public static String capitalize(String name) {
        if (name.length() == 1) {
            return name.toUpperCase();
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    public static String lower(String s) {
        return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    public static String getCodeString(String s) {
        if (s == null) {
            return "null";
        }

        return "\"" + s + "\"";
    }

    public static String getTypedParams(List<String> constructorParameters) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < constructorParameters.size(); i++) {
            String param = constructorParameters.get(i);
            params.append(getSimpleName(param));
            params.append(" ");
            params.append(lower(getSimpleName(param)));
            if (i <= constructorParameters.size() - 2) {
                params.append(", ");
            }
        }
        return params.toString();
    }

    public static String getParams(List<String> parameters) {
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < parameters.size(); i++) {
            String dependency = parameters.get(i);
            params.append(lower(getSimpleName(dependency)));
            if (i < parameters.size() - 1) {
                params.append(", ");
            }
        }
        return params.toString();
    }

    public static String getCombinedClassName(String simpleViewClass, String simpleDataCLass) {
        return simpleViewClass + ":" + simpleDataCLass;
    }

    public static String getCleanPath(String path) {
        String result = path;

        if (result.contains(".")) {
            result = result.substring(result.indexOf(".") + 1);
        }

        if (result.contains(":")) {
            result = result.substring(0, result.indexOf(":"));
        }

        return result;
    }

    public static String getDataClassFromPath(String path) {
        if (path.contains(".")) {
            return path.substring(0, path.indexOf("."));
        }

        return path;
    }

    public static String getTargetDataClassFromPath(String path) {
        if (path.contains(":")) {
            return path.substring(path.indexOf(":") + 1);
        }

        return path;
    }

    public static String getSimpleName(String className) {
        if (className.contains(".")) {
            return className.substring(className.lastIndexOf(".") + 1);
        }
        return className;
    }

}
