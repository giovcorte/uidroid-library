package com.uidroid.uidroid.model;

import android.widget.EditText;

import com.uidroid.annotation.UI;

@UI.ViewConfiguration(view = EditText.class)
public class EditTextElement {

    @UI.Param(key = "text")
    public String text;

    @UI.Param(key = "face")
    public Integer face;

    @UI.Param(key = "gravity")
    public Integer gravity;

    @UI.Param(key = "color")
    public Integer color;

    @UI.Id
    public String id;

}
