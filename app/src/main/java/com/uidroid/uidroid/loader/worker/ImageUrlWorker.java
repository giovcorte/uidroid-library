package com.uidroid.uidroid.loader.worker;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.uidroid.uidroid.loader.ImageLoader;
import com.uidroid.uidroid.loader.ImageRequest;
import com.uidroid.uidroid.loader.cache.ImageCache;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageUrlWorker extends ImageWorker {

    public ImageUrlWorker(ImageView imageView, ImageRequest request, ImageLoader.ImageCallback callback, ImageCache imageCache) {
        super(imageView, request, callback, imageCache, ImageCache.CachingStrategy.ALL);
    }

    @Override
    protected Bitmap getBitmap(String source) {
        return downloadBitmap(source);
    }

    private Bitmap downloadBitmap(String url) {
        try {
            Bitmap bitmap;

            // InputStream from url
            final URL imageUrl = new URL(url);
            final HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            final InputStream inputStream = conn.getInputStream();

            // Creating byteArrayOutputStream to decode bitmap and cache it
            ByteArrayOutputStream outputStreamUrl = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                outputStreamUrl.write(buffer, 0, len);
            }
            outputStreamUrl.flush();
            inputStream.close();
            conn.disconnect();

            // Decode byte[] to bitmap, but not from the cached file. Doing so permit us to get the bitmap also if memory is full
            bitmap = decodeByteArray(outputStreamUrl.toByteArray(), request.getRequiredSize());
            outputStreamUrl.close();

            return bitmap;
        } catch (Throwable e) {
            if (e instanceof OutOfMemoryError) {
                cache.clear();
            }
            return null;
        }
    }
}
