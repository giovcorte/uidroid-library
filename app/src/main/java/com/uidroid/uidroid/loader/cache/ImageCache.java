package com.uidroid.uidroid.loader.cache;

import android.content.Context;
import android.graphics.Bitmap;

import com.uidroid.uidroid.loader.cache.disklrucache.DiskLruImageCache;
import com.uidroid.uidroid.loader.cache.memorycache.MemoryImageCache;

public class ImageCache {

    private static final String IMAGE_CACHE_NAME = "IImageCache";
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 250; // 250 mb

    private final IImageCache diskLruImageCache;
    private final IImageCache memoryImageCache;

    public enum CachingStrategy {
        ALL,
        MEMORY,
        DISK,
        NONE
    }

    public ImageCache(Context context) {
        memoryImageCache = new MemoryImageCache();
        diskLruImageCache = new DiskLruImageCache(context, IMAGE_CACHE_NAME, DISK_CACHE_SIZE);
    }

    public Bitmap get(String s) {
        return memoryImageCache.get(s) != null ? memoryImageCache.get(s) : diskLruImageCache.get(s);
    }

    public void put(String s, Bitmap data, CachingStrategy cachingStrategy) {
        switch (cachingStrategy) {
            case ALL:
                if (memoryImageCache.get(s) == null) {
                    memoryImageCache.put(s, data);
                }
                if (diskLruImageCache.get(s) == null) {
                    diskLruImageCache.put(s, data);
                }
                break;
            case DISK:
                if (diskLruImageCache.get(s) == null) {
                    diskLruImageCache.put(s, data);
                }
                break;
            case MEMORY:
                if (memoryImageCache.get(s) == null) {
                    memoryImageCache.put(s, data);
                }
                break;
            case NONE:
                break;
        }
    }

    public void clear() {
        memoryImageCache.clear();
        diskLruImageCache.clear();
    }

}
