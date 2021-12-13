package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;

public class Configuration implements UIField {

    String fieldName;
    String key;

    public Configuration(
            String fieldName,
            String key) {
        this.fieldName = fieldName;
        this.key = key;
    }

    @Override
    public void printCode(PrintWriter out) {
        String child = "this.build(value." + fieldName + ")";
        out.print("    object.addChildConfiguration(" + getCodeParams(getCodeString(key), child) + "); \n");
    }

}
