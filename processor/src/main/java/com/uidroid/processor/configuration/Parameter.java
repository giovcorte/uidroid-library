package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;

public class Parameter implements UIField {

    String fieldName;
    String key;

    public Parameter(String fieldName, String key) {
        this.fieldName = fieldName;
        this.key = key;
    }

    @Override
    public void printCode(PrintWriter out) {
        String child = "value." + fieldName;
        out.println("    object.putParam("
                + getCodeParams(getCodeString(key), child)
                + "); \n");
    }

}
