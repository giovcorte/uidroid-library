package com.uidroid.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface UI {

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface ViewConfiguration {
        Class<?> view();
        StableParam[] params() default {};
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface FieldConfiguration {
        String key() default "";
        Class<?> view();
        String id() default "";
        StableParam[] params() default {};
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Configuration {
        String key() default "";
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface ConfigurationsList {
        String key() default "";
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Param {
        String key() default "";
    }

    @Retention(RetentionPolicy.SOURCE)
    @interface StableParam {
        String key();
        String value();
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Action {

    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface Id {

    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface CustomView {

    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.FIELD)
    @interface View {
        String key() default "";
        int fallback() default 2;
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface BinderFor {
        Class<?> view();
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface Binder {

    }

    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE, ElementType.FIELD})
    @interface BindWith {
        Class<?> binder();
    }

    @Retention(RetentionPolicy.SOURCE)
    @Target(ElementType.TYPE)
    @interface AppPackage {
        String packageName();
    }

}
