package com.uidroid.uidroid.model;

import static com.uidroid.uidroid.binder.TextViewBinder.*;

import android.widget.TextView;

import com.uidroid.annotation.UI;

@UI.ViewConfiguration(view = TextView.class)
public class TextViewElement {

    @UI.Param(key = TEXT)
    public String text;

    @UI.Param(key = FACE)
    public Integer face;

    @UI.Param(key = GRAVITY)
    public Integer gravity;

    @UI.Param(key = COLOR)
    public Integer color;

}
