package ru.yandex.cloud.graphql.gateway.loader;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dataloader.BatchLoaderWithContext;
import org.springframework.lang.NonNull;

public class BatchLoaderRegistry {
    private final Map<Object, BatchLoaderWithContext> loaders = new HashMap<>();

    public <K, V> void register(@NonNull Object key, @NonNull BatchLoaderWithContext<K, V> loader) {
        loaders.put(key, loader);
    }

    @NonNull
    public Map<Object, BatchLoaderWithContext> getLoaders() {
        return Collections.unmodifiableMap(loaders);
    }
}
