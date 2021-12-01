package com.uidroid.uidroid.factory;

import android.content.Context;
import android.view.View;

public interface IViewFactory {

    View build(Context context, String value);

}
