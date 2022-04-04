package com.uidroid.uidroid.test;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.uidroid.annotation.BindAction;
import com.uidroid.annotation.BindWith;
import com.uidroid.annotation.BindableView;
import com.uidroid.uidroid.IView;

@BindableView
@BindAction(paths = {"ExampleModel.action"})
public class ExampleView extends View implements IView {

    @BindWith(paths = {"ExampleModel.text:String", "ExampleModel2.obj.text:String"})
    public TextView text;
    @BindWith(paths = {"ExampleModel.text:String"})
    public ImageView image;

    public ExampleView(Context context) {
        super(context);
    }

    @Override
    public String name() {
        return "ExampleView";
    }

}
