package com.uidroid.uidroid.binder;

import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.model.TextViewElement;
import com.uidroid.uidroid.model.ViewConfiguration;

public class TextViewBinder<V extends TextView> extends ListenerViewBinder<V, EditTextViewBinder.EditTextListener> {

    @SuppressWarnings("unused")
    public static final class TextFace {
        public static final int BOLD = Typeface.BOLD;
        public static final int NORMAL = Typeface.NORMAL;
        public static final int ITALIC = Typeface.ITALIC;
    }

    @SuppressWarnings("unused")
    public static final class TextGravity {
        public static final int START = Gravity.START;
        public static final int CENTER = Gravity.CENTER;
        public static final int END = Gravity.END;
    }

    public static final String TEXT = "text";
    public static final String COLOR = "color";
    public static final String FACE = "face";
    public static final String GRAVITY = "gravity";

    public TextViewBinder() {
        super();
    }

    @Override
    public void doBind(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {
        handleText(view, configuration);
        handleFace(view, configuration);
        handleGravity(view, configuration);
        handleColor(view, configuration);
    }

    private void handleText(V view, ViewConfiguration configuration) {
        if (configuration.getStringParam(TEXT) != null) {
            view.setText(configuration.getStringParam(TEXT));
        } else {
            view.setVisibility(View.GONE);
        }
    }

    private void handleGravity(V view, ViewConfiguration configuration) {
        final Integer gravity = configuration.getIntegerParam(GRAVITY);

        if (gravity != null) {
            view.setGravity(gravity);
        }
    }

    private void handleColor(V view, ViewConfiguration configuration) {
        final Integer color = configuration.getIntegerParam(COLOR);

        if (color != null) {
            view.setTextColor(color);
        }
    }

    private void handleFace(V view, ViewConfiguration configuration) {
        final Integer face = configuration.getIntegerParam(FACE);

        if (face != null) {
            view.setTypeface(null, face);
        }
    }

    @Override
    public void doUnbind(V view, ViewConfiguration configuration, DatabindingContext databindingContext) {

    }

    @Override
    public void doRemove(DatabindingContext databindingContext, ViewConfiguration configuration) {

    }

}
