package com.uidroid.uidroid.loader.worker;

import android.graphics.Bitmap;
import android.widget.ImageView;

import com.uidroid.uidroid.loader.ImageLoader;
import com.uidroid.uidroid.loader.ImageRequest;
import com.uidroid.uidroid.loader.cache.ImageCache;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageFileWorker extends ImageWorker {

    public ImageFileWorker(ImageView imageView, ImageRequest request, ImageLoader.ImageCallback callback, ImageCache imageCache) {
        super(imageView, request, callback, imageCache, ImageCache.CachingStrategy.MEMORY);
    }

    @Override
    protected Bitmap getBitmap(String source) {
        File file = new File(source);

        try {
            FileInputStream inputStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            byteArrayOutputStream.flush();
            inputStream.close();
            Bitmap bitmap = decodeByteArray(byteArrayOutputStream.toByteArray(), request.getRequiredSize());
            byteArrayOutputStream.close();

            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
