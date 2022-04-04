package com.uidroid.processor.items;

public class BindableActionImpl {

    public String viewClassName;
    public String path;
    public String dataClassName;

    public BindableActionImpl(String viewClassName, String dataClassName, String path) {
        this.viewClassName = viewClassName;
        this.path = path;
        this.dataClassName = dataClassName;
    }

}
