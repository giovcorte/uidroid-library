package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationObject implements UIField {

    String binder;
    String viewType;
    String id;

    String fieldName;
    String key;

    public List<UIField> uiFields = new ArrayList<>();

    public ConfigurationObject() {

    }

    public ConfigurationObject(String viewType, String binder, String id, String fieldName, String key) {
        this.viewType = viewType;
        this.binder = binder;
        this.id = id;
        this.fieldName = fieldName;
        this.key = key;
    }

    @Override
    public void printCode(PrintWriter out) {
        final String variableName = "config" + fieldName;

        final String idCode = id != null && !id.equals("") ? getCodeString(id) : "String.valueOf(value." + fieldName + ".hashCode())";
        final String binderCode = binder == null ? "null" : getCodeString(binder);

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

}