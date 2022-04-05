package com.uidroid.uidroid.test;

import com.uidroid.annotation.BindableObject;
import com.uidroid.uidroid.IData;
import com.uidroid.uidroid.IViewAction;

@BindableObject(view = ExampleView.class)
public class ExampleModel implements IData {

    public String text;

    public IViewAction action;

    @Override
    public String name() {
        return "ExampleModel";
    }

    public String getText() {
        return text;
    }

    public IViewAction getAction() {
        return action;
    }
}
