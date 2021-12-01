package com.uidroid.uidroid.loader.cache.memorycache;

import android.graphics.Bitmap;

import com.uidroid.uidroid.loader.cache.IImageCache;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MemoryImageCache implements IImageCache {

    private final Map<String, Bitmap> cache = Collections.synchronizedMap(
            new LinkedHashMap<>(10, 1.5f, true));

    private long size = 0;
    private final long limit;

    public MemoryImageCache() {
        limit = Runtime.getRuntime().maxMemory() / 4;
    }

    public Bitmap get(String id) {
        try {
            if (!cache.containsKey(id)) {
                return null;
            }

            return cache.get(id);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @Override
    public boolean contains(String key) {
        return cache.containsKey(key);
    }

    public void put(String id, Bitmap bitmap) {
        if (cache.containsKey(id)) {
            size -= getSizeInBytes(cache.get(id));
        }

        cache.put(id, bitmap);
        size += getSizeInBytes(bitmap);

        checkSize();
    }

    private void checkSize() {
        if (size > limit) {
            // Least recently accessed item will be the first one iterated
            Iterator<Entry<String, Bitmap>> iterator = cache.entrySet().iterator();

            while (iterator.hasNext()) {
                Entry<String, Bitmap> entry = iterator.next();
                size -= getSizeInBytes(entry.getValue());
                iterator.remove();

                if (size <= limit) {
                    break;
                }
            }
        }
    }

    public void clear() {
        cache.clear();
        size = 0;
    }

    long getSizeInBytes(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }

        return (long) bitmap.getRowBytes() * bitmap.getHeight();
    }

}
