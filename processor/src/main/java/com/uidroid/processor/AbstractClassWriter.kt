package com.uidroid.processor

import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

abstract class AbstractClassWriter(var filer: Filer, private var messager: Messager) {
    /**
     * Error method
     */
    private fun error(message: String) {
        messager.printMessage(Diagnostic.Kind.ERROR, message)
    }
}