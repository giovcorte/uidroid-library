package com.uidroid.uidroid.factory;

import android.view.View;

import com.uidroid.uidroid.DatabindingException;
import com.uidroid.uidroid.view.IViewComposite;

public interface IViewCompositeFactory {

    IViewComposite build(View object) throws DatabindingException;

}
