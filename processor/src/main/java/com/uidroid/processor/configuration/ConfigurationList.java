package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;

public class ConfigurationList implements UIField {

    String key;
    String fieldName;

    public ConfigurationList(String key, String fieldName) {
        this.key = key;
        this.fieldName = fieldName;
    }

    @Override
    public void printCode(PrintWriter out) {
        out.print("    for (Object o: new ArrayList<Object>(value." + fieldName + ")) { \n");
        out.print("      object.addChildConfiguration(" + getCodeString(key) + ", this.build(o)); \n");
        out.print("    } \n");
    }

}
