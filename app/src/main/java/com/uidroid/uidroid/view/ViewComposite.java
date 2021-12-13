package com.uidroid.uidroid.view;

import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ViewComposite {

    private final Map<String, ViewCompositeChild> mapping = new LinkedHashMap<>();

    public static class ViewCompositeChild {
        public String key;
        public View view;
        public int fallback;

        public ViewCompositeChild(String key, View view, int fallback) {
            this.key = key;
            this.view = view;
            this.fallback = fallback;
        }

    }

    @SuppressWarnings("unused")
    public void put(String key, View view, int fallback) {
        mapping.put(key, new ViewCompositeChild(key, view, fallback));
    }

    public List<ViewCompositeChild> getSubViews() {
        return new ArrayList<>(mapping.values());
    }

    public ViewCompositeChild getSubView(String key) {
        return mapping.get(key);
    }

}
