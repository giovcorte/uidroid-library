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

    /**
     * Creates a new ViewConfiguration instance equals to this, but with the new id.
     *
     * @param id String new id.
     * @return ViewConfiguration instance.
     */
    public ViewConfiguration cloneWithId(String id) {
        return new ViewConfiguration(id, viewType, binderType, viewKey, parent, children, action, params);
    }

    /**
     * Returns the id for this configuration. The constructors makes impossible to instantiate a
     * ViewConfiguration object with a null id.
     *
     * @return String id.
     */
    @NonNull
    public String getId() {
        return viewId;
    }

    /**
     * Returns the class type representing the view to generate and bind to this configuration.
     *
     * @return String view type.
     */
    public String getViewType() {
        return viewType;
    }

    /**
     * Returns a specific class type for the desired binder class. If not specified, the fallback
     * will be on the view type value.
     *
     * @return String for the binder class type.
     */
    public String getBinderType() {
        return binderType;
    }

    /**
     * Returns the key associated to this configuration. If null this is a root configuration.
     *
     * @return String key.
     */
    public String getKey() {
        return viewKey;
    }

    /**
     * Returns the father of this configuration in the view tree.
     *
     * @return ViewConfiguration father.
     */
    public ViewConfiguration getParentConfiguration() {
        return this.parent;
    }

    /**
     * Checks if this configuration has a parent in the view tree.
     *
     * @return boolean.
     */
    public boolean hasParent() {
        return this.parent != null;
    }

    /**
     * Returns all the children of this configuration.
     *
     * @return List of the children.
     */
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

    /**
     * Returns the first available child with the given key, id any.
     *
     * @param key String representing the child.
     * @return ViewConfiguration child for the specified key.
     */
    public ViewConfiguration getChildConfigurationByKey(String key) {
        for (ViewConfiguration configuration: children) {
            if (configuration.getKey().equals(key)) {
                return configuration;
            }
        }

        return null;
    }

    /**
     * Returns the available child with the given id, if any.
     *
     * @param id String representing the child.
     * @return ViewConfiguration child for the specified key.
     */
    public ViewConfiguration getChildConfigurationById(String id) {
        for (ViewConfiguration configuration: children) {
            if (configuration.getId().equals(id)) {
                return configuration;
            }
        }

        return null;
    }

    /**
     * Adds a single ViewConfiguration object as a child of this configuration.
     *
     * @param key String key for identify the child.
     * @param configuration ViewConfiguration child.
     */
    public synchronized void addChildConfiguration(String key, ViewConfiguration configuration) {
        if (configuration != null) {
            configuration.parent = this;
            configuration.viewKey = key;

            children.add(configuration);
        }
    }

    /**
     * Removes the ViewConfiguration child which match the provided filter.
     *
     * @param filter IViewConfigurationFilter to filter the children.
     */
    public synchronized void removeChildByFilter(IViewConfigurationFilter filter) {
        if (filter != null) {
            for (ViewConfiguration configuration : children) {
                if (filter.match(configuration.getKey(), configuration)) {
                    children.remove(configuration);
                    break;
                }
            }
        }
    }

    /**
     * Removes the ViewConfiguration child at the provided position, in the sub-collection
     * identified by the provided filter.
     *
     * @param position int position of the child to remove.
     * @param filter IViewConfigurationFilter to filter the children.
     */
    public synchronized void removeChildByPosition(int position, IViewConfigurationFilter filter) {
        int i = 0;

        if (filter != null) {
            for (ViewConfiguration configuration : children) {
                if (filter.match(configuration.getKey(), configuration)) {
                    if (i == position) {
                        children.remove(configuration);
                        break;
                    }

                    i++;
                }
            }
        } else {
            for (ViewConfiguration configuration : children) {
                if (i == position) {
                    children.remove(configuration);
                    break;
                }

                i++;
            }
        }
    }

    /**
     * Returns the action to perform when the associated view is clicked.
     *
     * @return IViewAction instance.
     */
    public IViewAction getAction() {
        return action;
    }

    /**
     * Sets the IViewAction to perform.
     *
     * @param action IViewAction instance.
     */
    public void setAction(IViewAction action) {
        this.action = action;
    }

    /**
     * Adds the the children of this instance all the ViewConfigurations held in the provided list,
     * with the specified key.
     *
     * @param key String key for identify the provided collection.
     * @param list List of ViewConfiguration objects.
     */
    public synchronized void addChildrenConfigurations(String key, List<ViewConfiguration> list) {
        for (ViewConfiguration configuration : list) {
            if (configuration == null) {
                continue;
            }

            configuration.parent = this;
            configuration.viewKey = key;
        }
        children.addAll(list);
    }

    /**
     * Returns the children which keys match the given key.
     *
     * @param key String key of the children.
     * @return List of ViewConfiguration objects.
     */
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

    /**
     * Returns the children which match the given filter.
     *
     * @param filter IViewConfiguration filter.
     * @return List of ViewConfiguration objects.
     */
    public List<ViewConfiguration> getChildrenConfigurations(IViewConfigurationFilter filter) {
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
