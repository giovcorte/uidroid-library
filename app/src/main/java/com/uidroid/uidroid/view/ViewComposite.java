package com.uidroid.uidroid.view;

import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ViewComposite {

    private final Map<String, ChildView> mapping = new LinkedHashMap<>();

    public static class ChildView {
        public String key;
        public View view;
        public int fallback;

        public ChildView(String key, View view, int fallback) {
            this.key = key;
            this.view = view;
            this.fallback = fallback;
        }

    }

    @SuppressWarnings("unused")
    public void put(String key, View view, int fallback) {
        mapping.put(key, new ChildView(key, view, fallback));
    }

    public List<ChildView> getSubViews() {
        return new ArrayList<>(mapping.values());
    }

    public ChildView getSubView(String key) {
        return mapping.get(key);
    }

}
