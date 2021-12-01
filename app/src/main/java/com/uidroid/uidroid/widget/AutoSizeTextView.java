package com.uidroid.uidroid.widget;

import android.content.Context;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.binder.TextViewBinder;

@UI.CustomView
@UI.BindWith(binder = TextViewBinder.class)
public class AutoSizeTextView extends AppCompatTextView {

    public static final float MIN_TEXT_SIZE = 12;

    public interface IResizeListener {
        void onTextResize(TextView textView, float oldSize, float newSize);
    }

    private static final String ellipsis = "...";

    private IResizeListener resizeListener;

    private boolean resize = false;
    private boolean addEllipsis = true;

    private float textSize;
    private float maxTextSize = 0;
    private float minTextSize = MIN_TEXT_SIZE;
    private float spacingMult = 1.0f;
    private float spacingAdd = 0.0f;

    public AutoSizeTextView(Context context) {
        this(context, null);
    }

    public AutoSizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoSizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        textSize = getTextSize();
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        resize = true;
        resetTextSize();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw || h != oldh) {
            resize = true;
        }
    }

    public void setOnResizeListener(IResizeListener listener) {
        resizeListener = listener;
    }

    @Override
    public void setTextSize(float size) {
        super.setTextSize(size);
        textSize = getTextSize();
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        textSize = getTextSize();
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);

        spacingMult = mult;
        spacingAdd = add;
    }

    public void setMaxTextSize(float maxTextSize) {
        this.maxTextSize = maxTextSize;

        requestLayout();
        invalidate();
    }

    public float getMaxTextSize() {
        return maxTextSize;
    }

    public void setMinTextSize(float minTextSize) {
        this.minTextSize = minTextSize;

        requestLayout();
        invalidate();
    }

    public float getMinTextSize() {
        return minTextSize;
    }

    public void setAddEllipsis(boolean addEllipsis) {
        this.addEllipsis = addEllipsis;
    }

    public boolean getAddEllipsis() {
        return addEllipsis;
    }

    public void resetTextSize() {
        if (textSize > 0) {
            super.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            maxTextSize = textSize;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (changed || resize) {
            int widthLimit = (right - left) - getCompoundPaddingLeft() - getCompoundPaddingRight();
            int heightLimit = (bottom - top) - getCompoundPaddingBottom() - getCompoundPaddingTop();

            resizeText(widthLimit, heightLimit);
        }

        super.onLayout(changed, left, top, right, bottom);
    }

    public void resizeText() {
        final int heightLimit = getHeight() - getPaddingBottom() - getPaddingTop();
        final int widthLimit = getWidth() - getPaddingLeft() - getPaddingRight();

        resizeText(widthLimit, heightLimit);
    }

    public void resizeText(int width, int height) {
        CharSequence text = getText();
        if (text == null || text.length() == 0 || height <= 0 || width <= 0 || textSize == 0) {
            return;
        }

        if (getTransformationMethod() != null) {
            text = getTransformationMethod().getTransformation(text, this);
        }

        final TextPaint textPaint = getPaint();

        float oldTextSize = textPaint.getTextSize();
        float targetTextSize = maxTextSize > 0 ? Math.min(textSize, maxTextSize) : textSize;

        int textHeight = getTextHeight(text, textPaint, width, targetTextSize);

        while (textHeight > height && targetTextSize > minTextSize) {
            targetTextSize = Math.max(targetTextSize - 2, minTextSize);
            textHeight = getTextHeight(text, textPaint, width, targetTextSize);
        }

        if (addEllipsis && targetTextSize == minTextSize && textHeight > height) {
            final TextPaint paint = new TextPaint(textPaint);
            StaticLayout layout = new StaticLayout(
                    text,
                    paint,
                    width,
                    Layout.Alignment.ALIGN_NORMAL,
                    spacingMult,
                    spacingAdd,
                    false);

            if (layout.getLineCount() > 0) {
                int lastLine = layout.getLineForVertical(height) - 1;

                if (lastLine < 0) {
                    setText("");
                } else {
                    int start = layout.getLineStart(lastLine);
                    int end = layout.getLineEnd(lastLine);
                    float lineWidth = layout.getLineWidth(lastLine);
                    float ellipseWidth = textPaint.measureText(ellipsis);

                    while (width < lineWidth + ellipseWidth) {
                        lineWidth = textPaint.measureText(text.subSequence(start, --end + 1).toString());
                    }

                    setText(String.format("%s%s", text.subSequence(0, end), ellipsis));
                }
            }
        }

        setTextSize(TypedValue.COMPLEX_UNIT_PX, targetTextSize);
        setLineSpacing(spacingAdd, spacingMult);

        if (resizeListener != null) {
            resizeListener.onTextResize(this, oldTextSize, targetTextSize);
        }

        resize = false;
    }

    private int getTextHeight(CharSequence source, TextPaint paint, int width, float textSize) {
        final TextPaint paintCopy = new TextPaint(paint);
        paintCopy.setTextSize(textSize);

        final StaticLayout layout = new StaticLayout(
                source,
                paintCopy,
                width,
                Layout.Alignment.ALIGN_NORMAL,
                spacingMult,
                spacingAdd,
                true);

        return layout.getHeight();
    }

}
