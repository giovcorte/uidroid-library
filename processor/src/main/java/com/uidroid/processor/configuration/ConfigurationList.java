package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;

class ConfigurationList implements UIField {

    String key;
    String fieldName;

    ConfigurationList(String key, String fieldName) {
        this.key = key;
        this.fieldName = fieldName;
    }

    @Override
    public void printAddToConfigurationCode(PrintWriter out) {
        out.print("    for (Object o: new ArrayList<Object>(value." + fieldName + ")) { \n");
        out.print("      object.addChildConfiguration(" + getCodeString(key) + ", this.build(o)); \n");
        out.print("    } \n");
    }

}
