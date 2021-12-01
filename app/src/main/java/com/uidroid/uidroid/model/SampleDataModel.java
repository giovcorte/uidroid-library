package com.uidroid.uidroid.model;

import android.widget.TextView;

import com.uidroid.annotation.UI;
import com.uidroid.uidroid.binder.EditTextViewBinder;

import java.util.List;

@UI.BindWith(binder = EditTextViewBinder.class)
@UI.ViewConfiguration(view = TextView.class, params = {@UI.StableParam(key = "gravity", value = "1")})
public class SampleDataModel {

    @UI.Param(key = "text")
    public String text;

    @UI.ConfigurationsList
    public List<ImageViewElement> images;

    @UI.FieldConfiguration(view = TextView.class, params = {@UI.StableParam(key = "font", value = "bbbb")})
    public String textFormatted;

}
