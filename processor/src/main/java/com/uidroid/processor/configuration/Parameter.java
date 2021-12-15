package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;

class Parameter implements UIField {

    String fieldName;
    String key;

    Parameter(String fieldName, String key) {
        this.fieldName = fieldName;
        this.key = key;
    }

    @Override
    public void printAddToConfigurationCode(PrintWriter out) {
        String child = "value." + fieldName;
        out.println("    object.putParam("
                + getCodeParams(getCodeString(key), child)
                + "); \n");
    }

}
