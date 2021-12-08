package com.uidroid.uidroid.factory;

import com.uidroid.uidroid.DatabindingException;
import com.uidroid.uidroid.binder.IViewBinder;

public interface IViewBinderFactory {

    IViewBinder build(String value) throws DatabindingException;

}
