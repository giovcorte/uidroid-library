package com.uidroid.processor.configuration;

import static com.uidroid.processor.Utils.getCodeParams;
import static com.uidroid.processor.Utils.getCodeString;

import java.io.PrintWriter;

class StableParameter implements UIField {

    String value;
    String key;

    StableParameter(String key, String value) {
        this.value = value;
        this.key = key;
    }

    @Override
    public void printAddToConfigurationCode(PrintWriter out) {
        out.print("    object.putParam("
                + getCodeParams(getCodeString(key), getCodeString(value))
                + "); \n");
    }

}
