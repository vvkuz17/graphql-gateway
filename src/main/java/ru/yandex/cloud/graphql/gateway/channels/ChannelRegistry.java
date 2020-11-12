package ru.yandex.cloud.graphql.gateway.channels;

import java.util.HashMap;
import java.util.Map;

import org.springframework.lang.NonNull;

public class ChannelRegistry {
    private final Map<String, Channel> channels = new HashMap<>();

    public <T> void register(@NonNull String name, @NonNull Channel<T> channel) {
        channels.put(name, channel);
    }

    public <T> Channel<T> getChannel(String name) {
        return channels.get(name);
    }
}
