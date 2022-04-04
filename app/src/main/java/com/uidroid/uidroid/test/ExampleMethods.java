package com.uidroid.uidroid.test;

import android.widget.ImageView;
import android.widget.TextView;

import com.uidroid.annotation.BindingMethod;
import com.uidroid.annotation.Data;
import com.uidroid.annotation.Inject;
import com.uidroid.annotation.View;

public class ExampleMethods {

    @BindingMethod
    public static void bindExampleView(@View ExampleView view,
                                       @Data ExampleModel model,
                                       @Inject ExampleRepo exampleRepo) {

    }

    @BindingMethod
    public static void bindExampleView2(@View ExampleView view,
                                       @Data ExampleModel2 model,
                                       @Inject ExampleRepo exampleRepo) {

    }

    @BindingMethod
    public static void bindImageView(@View ImageView view, @Data String url) {

    }

    @BindingMethod
    public static void bindText(@View TextView view, @Data String text) {
        view.setText(text);
    }

    @BindingMethod
    public static void bindText(@View TextView view, @Data ExampleModel text) {
        view.setText(text.text);
    }
}
