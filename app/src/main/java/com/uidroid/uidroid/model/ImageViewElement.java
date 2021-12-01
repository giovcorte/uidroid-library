package com.uidroid.uidroid.model;

import static com.uidroid.uidroid.binder.ImageViewBinder.*;

import android.widget.ImageView;

import com.uidroid.annotation.UI;

@UI.ViewConfiguration(view = ImageView.class)
public class ImageViewElement {

    @UI.Param(key = URL)
    public String url;

    @UI.Param(key = RESOURCE_ID)
    public Integer resourceId;

    @UI.Param(key = FILE)
    public String file;

    @UI.Param(key = COLOR)
    public Integer color;

    @UI.Param(key = PLACE_HOLDER)
    public Integer placeHolder;

    @UI.Param(key = SIZE)
    public Integer size;

}
