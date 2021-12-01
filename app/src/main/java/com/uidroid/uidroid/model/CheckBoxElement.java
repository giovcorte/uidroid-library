package com.uidroid.uidroid.model;

import static com.uidroid.uidroid.binder.CheckBoxBinder.CHECKBOX_SELECTED;

import android.widget.CheckBox;

import com.uidroid.annotation.UI;

@UI.ViewConfiguration(view = CheckBox.class)
public class CheckBoxElement {

    @UI.Id
    public String id;

    @UI.Param(key = CHECKBOX_SELECTED)
    public boolean selected;

}
