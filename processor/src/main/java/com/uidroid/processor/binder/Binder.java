package com.uidroid.processor.binder;

import java.util.ArrayList;
import java.util.List;

class Binder {

    List<String> viewClassNames = new ArrayList<>();
    List<String> constructorParameters = new ArrayList<>();

    Binder() {

    }

    void addClass(String clazz) {
        if (!viewClassNames.contains(clazz)) {
            viewClassNames.add(clazz);
        }
    }

    Binder putClass(String clazz) {
        if (!viewClassNames.contains(clazz)) {
            viewClassNames.add(clazz);
        }
        return this;
    }

}
