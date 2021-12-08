package com.uidroid.uidroid.factory;

import android.content.Context;
import android.view.View;

import com.uidroid.uidroid.DatabindingException;

public interface IViewFactory {

    View build(Context context, String value) throws DatabindingException;

}
