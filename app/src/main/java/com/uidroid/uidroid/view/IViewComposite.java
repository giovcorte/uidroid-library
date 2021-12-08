package com.uidroid.uidroid.view;

import android.view.View;

import java.util.List;

@SuppressWarnings("unused")
public interface IViewComposite {

    interface IViewCompositeChild {
        String key();
        View view();
        int fallback();
    }

    void putChildView(String key, View view, int fallback);

    List<IViewCompositeChild> getChildrenViews();

    IViewCompositeChild getChildView(String key);

}
