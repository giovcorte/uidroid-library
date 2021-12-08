package com.uidroid.uidroid.loader.worker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import com.uidroid.uidroid.DatabindingContext;
import com.uidroid.uidroid.loader.ImageException;
import com.uidroid.uidroid.loader.ImageLoader;
import com.uidroid.uidroid.loader.ImageRequest;
import com.uidroid.uidroid.loader.cache.ImageCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public abstract class ImageWorker implements Callable<Void> {

    protected final ImageCache cache;
    protected final ImageLoader.ImageCallback callback;
    protected final ImageRequest request;
    protected final ImageView view;
    protected final ImageCache.CachingStrategy cachingStrategy;

    public ImageWorker(ImageView view,
                       ImageRequest request,
                       ImageLoader.ImageCallback callback,
                       ImageCache cache,
                       ImageCache.CachingStrategy cachingStrategy) {
        this.view = view;
        this.request = request;
        this.callback = callback;
        this.cache = cache;
        this.cachingStrategy = cachingStrategy;
    }

    @Override
    public Void call() {
        // Check if imageView is being reused
        if (view.getTag() == null || view.getTag() != request.getSource()) {
            return null;
        }

        Bitmap bitmap = getBitmap(request.getSource());

        if (bitmap == null) {
            callback.onError(new ImageException());
            return null;
        }

        cache.put(request.getSource(), bitmap, cachingStrategy);

        // Check if imageView is being reused
        if (view.getTag() == null || view.getTag() != request.getSource()) {
            return null;
        }

        callback.onSuccess(bitmap, view, request);
        return null;
    }

    protected abstract Bitmap getBitmap(String source);

    static Bitmap decodeByteArray(byte[] bytes, int requiredSize) {
        try {
            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream stream1 = new ByteArrayInputStream(bytes);
            BitmapFactory.decodeStream(stream1,null, options);
            stream1.close();
            // Scale image in order to reduce memory consumption
            int width_tmp = options.outWidth, height_tmp = options.outHeight;
            int scale = 1;
            while (width_tmp / 2 >= requiredSize && height_tmp / 2 >= requiredSize) {
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            // Decode with current scale values
            BitmapFactory.Options options1 = new BitmapFactory.Options();
            options1.inSampleSize = scale;
            InputStream stream2 = new ByteArrayInputStream(bytes);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, options1);
            stream2.close();

            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }

}
