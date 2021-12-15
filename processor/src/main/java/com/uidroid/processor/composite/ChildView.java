package com.uidroid.processor.composite;

class ChildView {

    int fallback;
    String fieldName;
    String key;

    ChildView(String fieldName, int fallback, String key) {
        this.fallback = fallback;
        this.fieldName = fieldName;
        this.key = key;
    }
}
