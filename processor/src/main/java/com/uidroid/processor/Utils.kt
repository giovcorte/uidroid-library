package com.uidroid.processor

import java.lang.StringBuilder

/**
 * Utility methods
 */
object Utils {

    fun capitalize(name: String): String {
        return if (name.length == 1) {
            name.toUpperCase()
        } else name.substring(0, 1).toUpperCase() + name.substring(1)
    }

    @JvmStatic
    fun lower(s: String): String {
        return Character.toLowerCase(s[0]).toString() + s.substring(1)
    }

    @JvmStatic
    fun getCodeString(s: String?): String {
        return if (s == null) {
            "null"
        } else "\"" + s + "\""
    }

    @JvmStatic
    fun getTypedParams(constructorParameters: List<String>): String {
        val params = StringBuilder()
        for (i in constructorParameters.indices) {
            val param = constructorParameters[i]
            params.append(getSimpleName(param))
            params.append(" ")
            params.append(lower(getSimpleName(param)))
            if (i <= constructorParameters.size - 2) {
                params.append(", ")
            }
        }
        return params.toString()
    }

    @JvmStatic
    fun getParams(parameters: List<String>): String {
        val params = StringBuilder()
        for (i in parameters.indices) {
            val dependency = parameters[i]
            params.append(lower(getSimpleName(dependency)))
            if (i < parameters.size - 1) {
                params.append(", ")
            }
        }
        return params.toString()
    }

    @JvmStatic
    fun getCombinedClassName(simpleViewClass: String, simpleDataCLass: String): String {
        return "$simpleViewClass:$simpleDataCLass"
    }

    @JvmStatic
    fun getCleanPath(path: String): String {
        var result = path
        if (result.contains(".")) {
            result = result.substring(result.indexOf(".") + 1)
        }
        if (result.contains(":")) {
            result = result.substring(0, result.indexOf(":"))
        }
        return result
    }

    @JvmStatic
    fun getDataClassFromPath(path: String): String {
        return if (path.contains(".")) {
            path.substring(0, path.indexOf("."))
        } else path
    }

    @JvmStatic
    fun getTargetDataClassFromPath(path: String): String {
        return if (path.contains(":")) {
            path.substring(path.indexOf(":") + 1)
        } else path
    }

    @JvmStatic
    fun getSimpleName(className: String): String {
        return if (className.contains(".")) {
            className.substring(className.lastIndexOf(".") + 1)
        } else className
    }
}