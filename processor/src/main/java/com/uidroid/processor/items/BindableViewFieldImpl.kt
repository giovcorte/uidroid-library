package com.uidroid.processor.items;

/**
 * Class which represents a @BindTo annotated view field.
 */
public class BindableViewFieldImpl {

    public String fieldName; // field name
    public String objectPath; // path without class names (the parent class name is held in the map of bindableViewFields)
    public String fieldViewClassName; // full class name of the view held in the parent view
    public String fieldObjectClassName; // simple class name of the object held in the parent data

    public BindableViewFieldImpl(String fieldName, String objectPath, String fieldViewClassName, String fieldObjectClassName) {
        this.fieldName = fieldName;
        this.objectPath = objectPath;
        this.fieldViewClassName = fieldViewClassName;
        this.fieldObjectClassName = fieldObjectClassName;
    }
}
