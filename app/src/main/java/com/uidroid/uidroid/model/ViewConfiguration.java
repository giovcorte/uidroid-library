package com.uidroid.uidroid.model;

import androidx.annotation.NonNull;

import com.uidroid.uidroid.handler.IViewAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class representing the data to display in a view. From the specific instance of ViewModel views
 * and binders are generated at runtime through a two-round visitor -> factory pattern.
 */
@SuppressWarnings("unused")
public final class ViewConfiguration {

    /**
     * Interface for define a filter based on key - configuration pair child of the
     * ViewConfiguration.
     */
    public interface IViewConfigurationFilter {
        boolean match(String key, ViewConfiguration configuration);
    }

    private final String viewId;
    private final String viewType;
    private final String binderType;

    private String viewKey;

    private ViewConfiguration parent = null;
    private final List<ViewConfiguration> children;
    private IViewAction action;

    /**
     *
     * @param id String id which will identify bindings, configuration and others component.
     * @param viewType String representing the type of the view to bind and generate (if necessary).
     * @param binderType String representing the overriding of the viewType for the binder type.
     */
    public ViewConfiguration(String id,
                             String viewType,
                             String binderType) {
        if (id == null) {
            throw new RuntimeException("ViewConfiguration id cannot be null");
        }

        this.binderType = binderType;
        this.viewType = viewType;
        this.viewId = id;
        this.children = Collections.synchronizedList(new ArrayList<>());
        this.params = new ConcurrentHashMap<>(new LinkedHashMap<>());
    }

    /**
     *
     * @param id String id which will identify bindings, configuration and others component.
     * @param viewType String representing the type of the view to bind and generate (if necessary).
     * @param binderType String representing the overriding of the viewType for the binder type.
     * @param key String which map this configuration in the children of the father (can be null).
     * @param parent ViewConfiguration which is the parent of this configuration in the view tree.
     * @param children List of ViewConfigurations children of this instance.
     * @param action IViewAction to be performed on click of the view.
     * @param params Miscellaneous objects which represents the effective data to display in the view.
     */
    private ViewConfiguration(String id,
                              String viewType,
                              String binderType,
                              String key,
                              ViewConfiguration parent,
                              List<ViewConfiguration> children,
                              IViewAction action,
                              Map<String, Object> params) {
        if (id == null) {
            throw new RuntimeException("ViewConfiguration id cannot be null");
        }

        this.binderType = binderType;
        this.viewType = viewType;
        this.viewId = id;
        this.parent = parent;
        this.children = children;
        this.action = action;
        this.params = params;
    }

    public ViewConfiguration cloneWithId(String id) {
        return new ViewConfiguration(id, viewType, binderType, viewKey, parent, children, action, params);
    }

    @NonNull
    public String getId() {
        return viewId;
    }

    public String getViewType() {
        return viewType;
    }

    public String getBinderType() {
        return binderType;
    }

    public String getKey() {
        return viewKey;
    }

    public ViewConfiguration getParentConfiguration() {
        return this.parent;
    }

    public boolean hasParent() {
        return this.parent != null;
    }

    public List<ViewConfiguration> getChildrenConfigurations() {
        return children;
    }

    public boolean hasChild(String key) {
        for (ViewConfiguration configuration: children) {
            if (configuration.getKey().equals(key)) {
                return true;
            }
        }

        return false;
    }

    public ViewConfiguration getChildConfigurationByKey(String key) {
        for (ViewConfiguration configuration: children) {
            if (configuration.getKey().equals(key)) {
                return configuration;
            }
        }

        return null;
    }

    public synchronized void addChildConfiguration(String key, ViewConfiguration configuration) {
        if (configuration != null) {
            configuration.parent = this;
            configuration.viewKey = key;

            children.add(configuration);
        }
    }

    public synchronized void removeChildByFilter(IViewConfigurationFilter filter) {
        for (ViewConfiguration configuration: children) {
            if (filter.match(configuration.getKey(), configuration)) {
                children.remove(configuration);
                break;
            }
        }
    }

    public IViewAction getAction() {
        return action;
    }

    public void setAction(IViewAction action) {
        this.action = action;
    }

    public synchronized void addChildrenConfiguration(String key, List<ViewConfiguration> list) {
        for (ViewConfiguration configuration : list) {
            if (configuration == null) {
                continue;
            }

            configuration.parent = this;
            configuration.viewKey = key;
        }
        children.addAll(list);
    }

    public List<ViewConfiguration> getChildrenConfigurations(String key) {
        if (key == null) {
            return children;
        }

        List<ViewConfiguration> results = new ArrayList<>();

        for (ViewConfiguration configuration: children) {
            if (configuration.getKey().equals(key)) {
                results.add(configuration);
            }
        }

        return results;
    }

    public List<ViewConfiguration> getChildrenConfiguration(IViewConfigurationFilter filter) {
        if (filter == null) {
            return children;
        }

        List<ViewConfiguration> results = new ArrayList<>();

        for (ViewConfiguration configuration: children) {
            if (filter.match(configuration.getKey(), configuration)) {
                results.add(configuration);
            }
        }

        return results;
    }

    /**
     * Helper methods for accessing simple data with cast safety
     */

    private Map<String, Object> params;

    public synchronized void putParam(String key, Object param) {
        if (param == null) {
            return;
        }

        if (params == null) {
            this.params = new ConcurrentHashMap<>(new LinkedHashMap<>());
        }

        this.params.put(key, param);
    }

    public Object getParam(String key) {
        return params.get(key);
    }

    public Object getParam(String key, Object defaultValue) {
        if (params.get(key) != null) {
            return params.get(key);
        } else {
            return defaultValue;
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public String getStringParam(String key) {
        final Object param = params.get(key);

        if (param instanceof String) {
            return (String) params.get(key);
        }

        return null;
    }

    public Integer getIntegerParam(String key) {
        final Object param = params.get(key);

        if (param instanceof Integer) {
            return (Integer) param;
        }
        if (param instanceof String) {
            try {
                return Integer.valueOf((String) param);
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    public Double getDoubleParam(String key) {
        final Object param = params.get(key);

        if (param instanceof Double) {
            return (Double) param;
        }
        if (param instanceof String) {
            try {
                return Double.valueOf((String) param);
            } catch (ClassCastException e) {
                return null;
            }
        }
        return null;
    }

    public String getStringParam(String key, String defaultValue) {
        final String s = getStringParam(key);

        return s != null ? s : defaultValue;
    }

    public double getDoubleParam(String key, double defaultValue) {
        final Double d = getDoubleParam(key);

        return d != null ? d : defaultValue;
    }

    public int getIntegerParam(String key, int defaultValue) {
        final Integer i = getIntegerParam(key);

        return i != null ? i : defaultValue;
    }

    public boolean getBooleanParam(String key, boolean defaultValue) {
        final Object param = params.get(key);

        if (param instanceof Boolean) {
            return (Boolean) param;
        }

        if (param instanceof String) {
            try {
                return Boolean.parseBoolean((String) param);
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }

        return defaultValue;
    }
}
