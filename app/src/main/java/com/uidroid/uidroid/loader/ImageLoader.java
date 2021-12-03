package com.uidroid.uidroid.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Patterns;
import android.widget.ImageView;

import androidx.core.os.HandlerCompat;

import com.uidroid.uidroid.Utils;
import com.uidroid.uidroid.loader.cache.ImageCache;
import com.uidroid.uidroid.loader.worker.ImageFileWorker;
import com.uidroid.uidroid.loader.worker.ImageResourceWorker;
import com.uidroid.uidroid.loader.worker.ImageUrlWorker;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public final class ImageLoader implements IImageLoader {

    private static final int NUMBER_OF_THREADS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executorService;
    private final Handler handler;

    private final ImageCache cache;
    private final Map<String, FutureTask<Void>> tasks = new HashMap<>();

    public interface ImageCallback {
        void onSuccess(Bitmap bitmap, ImageView imageView, ImageRequest imageRequest);
        void onError(Throwable exception);
    }

    public ImageLoader(Context context) {
        cache = new ImageCache(context);
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
        handler = HandlerCompat.createAsync(Looper.getMainLooper());
    }

    public void load(ImageView view, ImageRequest request) {
        view.setTag(request.getSource());

        Bitmap bitmap = cache.get(request.getSource());
        if (bitmap != null) {
            view.setImageBitmap(bitmap);
            return;
        }

        view.setScaleType(request.getScaleType());
        view.setImageResource(request.getPlaceHolder());

        final ImageCallback callback = new ImageCallback() {
            @Override
            public void onSuccess(Bitmap bitmap, ImageView view, ImageRequest request) {
                if (view.getTag() == null || view.getTag() != request.getSource()) {
                    tasks.remove(request.getSource());
                }

                handler.post(() -> {
                    if (bitmap != null) {
                        view.setImageBitmap(bitmap);
                    } else {
                        view.setImageResource(request.getPlaceHolder());
                    }

                    tasks.remove(request.getSource());
                });
            }

            @Override
            public void onError(Throwable exception) {
                tasks.remove(request.getSource());
            }
        };

        final FutureTask<Void> worker = new FutureTask<>(
                ImageWorkerFactory.getWorker(view, request, callback, cache)
        );
        tasks.put(request.getSource(), worker);
        executorService.submit(worker);
    }

    public synchronized void cancel(String id) {
        final FutureTask<Void> task = tasks.get(id);

        if (task != null) {
            task.cancel(true);
        }
    }

    private final static class ImageWorkerFactory {

        private static Callable<Void> getWorker(ImageView view,
                                        ImageRequest request,
                                        ImageCallback callback,
                                        ImageCache cache) {
            final String source = request.getSource();

            if (isUrl(source)) {
                return new ImageUrlWorker(view, request, callback, cache);
            }

            if (isFile(source)) {
                return new ImageFileWorker(view, request, callback, cache);
            }

            if (isResource(source)) {
                return new ImageResourceWorker(view, request, callback, cache);
            }

            return null;
        }

        private static boolean isUrl(String source) {
            return Patterns.WEB_URL.matcher(source).matches();
        }

        private static boolean isFile(String source) {
            final File file = new File(source);

            return file.exists();
        }

        private static boolean isResource(String source) {
            return Utils.isInteger(source);
        }

    }

    @SuppressWarnings("unused")
    public void clearCache() {
        cache.clear();
    }

}
