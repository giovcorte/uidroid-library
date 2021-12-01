package com.uidroid.uidroid.loader.worker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import com.uidroid.uidroid.loader.ImageLoader;
import com.uidroid.uidroid.loader.ImageRequest;
import com.uidroid.uidroid.loader.cache.ImageCache;

public class ImageResourceWorker extends ImageWorker {

    public ImageResourceWorker(ImageView imageView, ImageRequest request, ImageLoader.ImageCallback callback, ImageCache imageCache) {
        super(imageView, request, callback, imageCache, ImageCache.CachingStrategy.MEMORY);
    }

    @Override
    protected Bitmap getBitmap(String source) {
        try {
            int resource = Integer.parseInt(source);

            Bitmap bitmap = BitmapFactory.decodeResource(view.getContext().getResources(),
                    resource);

            if (bitmap != null) {
                return bitmap;
            }

            Drawable drawable = ResourcesCompat.getDrawable(view.getResources(), resource, null);
            Canvas canvas = new Canvas();

            if (drawable != null) {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                canvas.setBitmap(bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                drawable.draw(canvas);
            }

            return bitmap;
        } catch (ClassCastException | NumberFormatException e) {
            return null;
        }
    }

}
