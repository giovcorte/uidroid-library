package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;

public class StableParameter implements UIField {

    String value;
    String key;

    public StableParameter(String key, String value) {
        this.value = value;
        this.key = key;
    }

    @Override
    public void printCode(PrintWriter out) {
        out.print("    object.putParam("
                + getCodeParams(getCodeString(key), getCodeString(value))
                + "); \n");
    }

}
