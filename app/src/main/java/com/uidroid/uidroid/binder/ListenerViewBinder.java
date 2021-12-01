package com.uidroid.uidroid.binder;

import android.view.View;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.model.ViewConfiguration;

public abstract class ListenerViewBinder<V extends View, L> extends ViewBinder<V> {

    private L listener = null;

    public synchronized void subscribeListener(L listener) {
        this.listener = listener;
    }

    public L getListener() {
        return this.listener;
    }

    public synchronized void unsubscribeListener() {
        this.listener = null;
    }

    @Override
    public void unbindView(DatabindingContext databindingContext, ViewConfiguration configuration, View view) {
        unsubscribeListener();
        super.unbindView(databindingContext, configuration, view);
    }

    @Override
    public void removeView(DatabindingContext databindingContext, ViewConfiguration configuration) {
        unsubscribeListener();
        super.removeView(databindingContext, configuration);
    }
}
