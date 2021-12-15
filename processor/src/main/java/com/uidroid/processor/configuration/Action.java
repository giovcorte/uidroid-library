package com.uidroid.processor.configuration;

import java.io.PrintWriter;

class Action implements UIField {

    String fieldName;

    Action(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public void printAddToConfigurationCode(PrintWriter out) {
        out.print("    object.setAction(value." + fieldName + ");");
    }

}
