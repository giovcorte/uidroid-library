package com.uidroid.processor.items;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class which represents a @BindableView annotated android view.
 */
public class BindableViewImpl {

    public String className;

    // Action for each object which can bind this view for this view
    public Map<String, BindableActionImpl> actions;

    // maps the simple class name of object which is binding  this view to the fields which path derives from this object
    public Map<String, List<BindableViewFieldImpl>> bindableViewFields;
    public Map<String, List<BindableActionFieldImpl>> bindableActionFields;

    public boolean implementIView;

    public BindableViewImpl(String className) {
        this.className = className;
        this.bindableViewFields = new LinkedHashMap<>();
        this.actions = new LinkedHashMap<>();
        this.bindableActionFields = new LinkedHashMap<>();
    }

    public BindableViewImpl(String className, boolean implementIView) {
        this.className = className;
        this.bindableViewFields = new LinkedHashMap<>();
        this.actions = new LinkedHashMap<>();
        this.implementIView = implementIView;
        this.bindableActionFields = new LinkedHashMap<>();
    }

}
