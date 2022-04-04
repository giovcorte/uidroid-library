package com.uidroid.processor.items;

import java.util.ArrayList;
import java.util.List;

public class BindingMethodImpl {

    public String enclosingClass;
    public String methodName;
    public String viewClass;
    public String dataClass;

    public List<String> dependencies = new ArrayList<>();

    public BindingMethodImpl(String enclosingClass, String methodName, String viewClass, String dataClass) {
        this.enclosingClass = enclosingClass;
        this.methodName = methodName;
        this.viewClass = viewClass;
        this.dataClass = dataClass;
    }

    public BindingMethodImpl(String enclosingClass, String methodName, String viewClass, String dataClass, List<String> dependencies) {
        this.enclosingClass = enclosingClass;
        this.methodName = methodName;
        this.viewClass = viewClass;
        this.dataClass = dataClass;
        this.dependencies = dependencies;
    }
}
