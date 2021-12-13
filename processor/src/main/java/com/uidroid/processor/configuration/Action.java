package com.uidroid.processor.configuration;

import java.io.PrintWriter;

public class Action implements UIField {

    String fieldName;

    public Action(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public void printCode(PrintWriter out) {
        out.print("    object.setAction(value." + fieldName + ");");
    }

}
