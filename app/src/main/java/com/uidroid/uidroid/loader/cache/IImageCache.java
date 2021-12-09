package com.uidroid.uidroid.loader.cache;

import android.graphics.Bitmap;

public interface IImageCache {

    Bitmap get(String key);

    @SuppressWarnings("unused")
    boolean contains(String key);

    void put(String key, Bitmap bitmap);
    void clear();

}
