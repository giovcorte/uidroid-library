package com.uidroid.uidroid.binder;

import android.view.View;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.DataBindingLogger;
import com.uidroid.uidroid.model.ViewConfiguration;

public abstract class ViewBinder<V extends View> implements IViewBinder {

    public ViewBinder() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void bindView(DatabindingContext databindingContext, ViewConfiguration configuration, View view) {
        try {
            final V component = (V) view;
            doBind(component, configuration, databindingContext);
        } catch (ClassCastException e) {
            DataBindingLogger.log(DataBindingLogger.Level.ERROR,
                    "Binding failed, view cast not compatible");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void unbindView(DatabindingContext databindingContext, ViewConfiguration configuration, View view) {
        try {
            final V component = (V) view;
            doUnbind(component, configuration, databindingContext);
        } catch (ClassCastException e) {
            DataBindingLogger.log(DataBindingLogger.Level.ERROR,
                    "Binding failed, view cast not compatible");
        }
    }

    @Override
    public void removeView(DatabindingContext databindingContext, ViewConfiguration configuration) {
        doRemove(databindingContext, configuration);
    }

    public abstract void doBind(V view, ViewConfiguration configuration, DatabindingContext databindingContext);

    public abstract void doUnbind(V view, ViewConfiguration configuration, DatabindingContext databindingContext);

    public abstract void doRemove(DatabindingContext databindingContext, ViewConfiguration configuration);

}
