package com.uidroid.uidroid.binder;

import android.view.View;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.model.ViewConfiguration;

public interface IViewBinder {

    void bindView(DatabindingContext databindingContext, ViewConfiguration configuration, View view);
    void unbindView(DatabindingContext databindingContext, ViewConfiguration configuration, View view);
    void removeView(DatabindingContext databindingContext, ViewConfiguration configuration);

}

