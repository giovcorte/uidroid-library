package com.uidroid.uidroid.view;

import android.view.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ViewComposite implements IViewComposite {

    private final Map<String, CompositeChild> mapping = new LinkedHashMap<>();

    public static class CompositeChild implements IViewCompositeChild{
        public String key;
        public View view;
        public int fallback;

        public CompositeChild(String key, View view, int fallback) {
            this.key = key;
            this.view = view;
            this.fallback = fallback;
        }

        @Override
        public String key() {
            return key;
        }

        @Override
        public View view() {
            return view;
        }

        @Override
        public int fallback() {
            return fallback;
        }
    }

    @Override
    public void putChildView(String key, View view, int fallback) {
        if (view != null) {
            mapping.put(key, new CompositeChild(key, view, fallback));
        }
    }

    @Override
    public List<IViewCompositeChild> getChildrenViews() {
        return new ArrayList<>(mapping.values());
    }

    @Override
    public CompositeChild getChildView(String key) {
        return mapping.get(key);
    }

}
