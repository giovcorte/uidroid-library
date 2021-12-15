package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.capitalize;
import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing an ViewConfiguration annotated object or a field annotated with FieldConfiguration.
 */
class ConfigurationObject implements UIField {

    String binderType;
    String viewType;
    String id;

    String fieldName;
    String key;

    List<UIField> uiFields = new ArrayList<>();

    ConfigurationObject() {

    }

    ConfigurationObject(String viewType, String binderType, String id, String fieldName, String key) {
        this.viewType = viewType;
        this.binderType = binderType;
        this.id = id;
        this.fieldName = fieldName;
        this.key = key;
    }

    public void printConstructorCode(PrintWriter out) {
        if (id != null) {
            out.print("    final String id = value." + id + " != null ? value." + id + " : String.valueOf(value.hashCode()); \n");
        } else {
            out.print("    final String id = String.valueOf(value.hashCode()); \n");
        }
        out.print("    ViewConfiguration object = new ViewConfiguration("
                + getCodeParams("id", getCodeString(viewType), binderType != null ? getCodeString(binderType) : "null")
                + "); \n");
    }

    @Override
    public void printAddToConfigurationCode(PrintWriter out) {
        final String variableName = "config" + capitalize(fieldName);

        final String idCode = id != null && !id.equals("") ? getCodeString(id) : "String.valueOf(value." + fieldName + ".hashCode())";
        final String binderCode = binderType == null ? "null" : getCodeString(binderType);

        out.print("    ViewConfiguration " + variableName + " = new ViewConfiguration("
                + getCodeParams(idCode, getCodeString(viewType), binderCode)
                + "); \n");

        out.print("    " + variableName + ".putParam("
                + getCodeString(key) + ", value." + fieldName
                + "); \n");

        for (UIField field: uiFields) {
            if (field instanceof StableParameter) {
                StableParameter param = (StableParameter) field;
                out.print("    " + variableName + ".putParam("
                        + getCodeString(param.key) + ", " + getCodeString(param.value)
                        + "); \n");
            }
        }
        out.print("    object.addChildConfiguration("
                + getCodeParams(getCodeString(key), variableName)
                + "); \n");
    }

    public void printReturnCode(PrintWriter out) {
        out.print("    return object; \n");
    }

}