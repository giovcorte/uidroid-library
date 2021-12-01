package com.uidroid.uidroid;

import android.util.Log;

/**
 * Class which simple logs DatabindingContext messages for debugging purposes.
 * Two level of severity are defined in this logging environment. The information level notifies
 * information about the binding operations such as if a view hasn't a registered binder (it's
 * totally fine).
 * The error level notifies exceptions that lead to inconsistencies in your ui, such as incompatible
 * casts at binding time or a view which cannot be generated.
 */
public final class DatabindingLogger {

    public enum Level {
        NONE,
        INFO,
        ERROR;

        int value() {
            switch (this) {
                case ERROR:
                    return 2;
                case INFO:
                    return 1;
                case NONE:
                default:
                    return 0;
            }
        }
    }

    /**
     * Default logging level
     */
    private static Level level = Level.INFO;

    public static void log(Level level, String message) {
        if (DatabindingLogger.level == Level.NONE) {
            return;
        }

        if (DatabindingLogger.level.value() >= level.value()
                && DatabindingLogger.level.value() > 0) {
            Log.d(DatabindingContext.DATABINDING_CONTEXT_TAG, message);
        }
    }

    @SuppressWarnings("unused")
    public static void setLevel(Level level) {
        DatabindingLogger.level = level;
    }

}
