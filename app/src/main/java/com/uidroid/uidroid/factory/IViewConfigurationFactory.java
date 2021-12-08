package com.uidroid.uidroid.factory;

import com.uidroid.uidroid.DatabindingException;
import com.uidroid.uidroid.model.ViewConfiguration;

public interface IViewConfigurationFactory {

    ViewConfiguration build(Object object) throws DatabindingException;

}
