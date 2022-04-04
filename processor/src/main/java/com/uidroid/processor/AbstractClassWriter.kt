package com.uidroid.processor;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

public abstract class AbstractClassWriter {

    protected Filer filer;
    protected Messager messager;

    public AbstractClassWriter(Filer filer, Messager messager) {
        this.filer = filer;
        this.messager = messager;
    }

    /**
     * Error method
     */
    private void error(String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message);
    }

}
