package com.uidroid.uidroid.model;

import androidx.annotation.NonNull;

import com.uidroid.uidroid.handler.IViewAction;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
public interface IViewConfiguration {

    interface IViewConfigurationFilter {
        boolean match(IViewConfiguration configuration);
    }

    @NonNull
    String id();

    String view();

    String binder();

    String key();

    IViewConfiguration getParentConfiguration();

    IViewConfiguration getChildConfigurationByKey(String key);

    IViewConfiguration getChildConfigurationById(String id);

    List<IViewConfiguration> getChildrenConfigurations();

    List<IViewConfiguration> getChildrenConfigurations(String key);

    List<IViewConfiguration> getChildrenConfigurations(IViewConfigurationFilter filter);

    boolean hasParent();

    boolean hasChild(String key);

    boolean hasChildrenList(String key);

    void addChildConfiguration(String key, IViewConfiguration configuration);

    void addChildrenConfigurations(String key, List<IViewConfiguration> list);

    void removeChildByFilter(IViewConfigurationFilter filter);

    void removeChildByPosition(int position, IViewConfigurationFilter filter);

    IViewAction getAction();

    void setAction(IViewAction action);

    /**
     * Parameters for data.
     */

    void putParam(String key, Object param);

    Object getParam(String key);

    Object getParam(String key, Object defaultValue);

    Map<String, Object> getParams();

    String getStringParam(String key);

    Integer getIntegerParam(String key);

    Double getDoubleParam(String key);

    String getStringParam(String key, String defaultValue);

    double getDoubleParam(String key, double defaultValue);

    int getIntegerParam(String key, int defaultValue);

    boolean getBooleanParam(String key, boolean defaultValue);

}
