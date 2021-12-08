package com.uidroid.uidroid.loader.cache.disklrucache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.uidroid.uidroid.DatabindingLogger;
import com.uidroid.uidroid.loader.cache.IImageCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DiskLruImageCache implements IImageCache {

    private DiskLruCache diskLruCache;

    private final Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;

    public DiskLruImageCache(Context context, String uniqueName, int diskCacheSize) {
        try {
            final File diskCacheDir = getDiskCacheDir(context, uniqueName);
            diskLruCache = DiskLruCache.open(diskCacheDir, 1, 1, diskCacheSize);
        } catch (IOException e) {
            DatabindingLogger.log(DatabindingLogger.Level.ERROR, "Cannot instantiate disk cache");
        }
    }

    @Override
    public void put(String key, Bitmap data) {
        if (diskLruCache == null) {
            return;
        }

        final String formattedKey = getDiskLruCacheFormattedString(key);

        DiskLruCache.Editor editor = null;

        try {
            editor = diskLruCache.edit(formattedKey);
            if (editor == null) {
                return;
            }

            if (writeBitmapToFile(data, editor)) {
                diskLruCache.flush();
                editor.commit();

                DatabindingLogger.log(DatabindingLogger.Level.INFO, "image put on disk cache " + key);
            } else {
                editor.abort();

                DatabindingLogger.log(DatabindingLogger.Level.INFO, "ERROR on: image put on disk cache " + key);
            }
        } catch (IOException e) {
            DatabindingLogger.log(DatabindingLogger.Level.INFO, "ERROR on: image put on disk cache " + key);

            try {
                if (editor != null) {
                    editor.abort();
                }
            } catch (IOException ignored) {

            }
        }

    }

    @Override
    public Bitmap get(String key) {
        final String formattedKey = getDiskLruCacheFormattedString(key);

        Bitmap bitmap = null;

        try (DiskLruCache.Snapshot snapshot = diskLruCache.get(formattedKey)) {
            if (snapshot == null) {
                return null;
            }

            final InputStream in = snapshot.getInputStream(0);

            if (in != null) {
                final BufferedInputStream buffIn =
                        new BufferedInputStream(in, 8 * 1024);

                bitmap = BitmapFactory.decodeStream(buffIn);
            }
        } catch (IOException | NullPointerException e) {
            DatabindingLogger.log(DatabindingLogger.Level.ERROR, "Cannot use disk cache");
        }
        DatabindingLogger.log(DatabindingLogger.Level.INFO, bitmap == null ? "" : "image read from disk " + key);

        return bitmap;
    }

    @Override
    public boolean contains(String key) {
        boolean contained = false;

        try (DiskLruCache.Snapshot snapshot = diskLruCache.get(key)) {
            contained = snapshot != null;
        } catch (IOException | NullPointerException ignored) {

        }

        return contained;
    }

    @Override
    public void clear() {
        try {
            diskLruCache.delete();
        } catch (IOException | NullPointerException e) {
            DatabindingLogger.log(DatabindingLogger.Level.ERROR, "Cannot clear cache");
        }
    }

    private boolean writeBitmapToFile( Bitmap bitmap, DiskLruCache.Editor editor ) {
        try (OutputStream out = new BufferedOutputStream(editor.newOutputStream(0), 1024 * 8)) {
            final int mCompressQuality = 70;

            return bitmap.compress(compressFormat, mCompressQuality, out);
        } catch (IOException e) {
            return false;
        }
    }

    private static String getDiskLruCacheFormattedString(String str) {
        String formatted = str.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();

        return formatted.substring(0, str.length() >= 120 ? 110 : str.length());
    }

    private File getDiskCacheDir(Context context, String uniqueName) {
        final String cachePath = context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

}

