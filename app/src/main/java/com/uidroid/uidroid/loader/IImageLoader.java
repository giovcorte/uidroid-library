package com.uidroid.uidroid.loader;

import android.widget.ImageView;

public interface IImageLoader {

    void load(ImageView view, ImageRequest image);

    void cancel(String id);
    void cancelAll();

    void clearCache();

}
