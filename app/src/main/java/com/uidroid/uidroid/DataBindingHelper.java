package com.uidroid.uidroid;

import android.view.View;

public final class DataBindingHelper {

    public static void bindAction(View view, IViewAction action) {
        view.setOnClickListener(v -> action.onClick());
    }

}
