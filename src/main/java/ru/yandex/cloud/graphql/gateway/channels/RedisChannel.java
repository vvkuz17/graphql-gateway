package ru.yandex.cloud.graphql.gateway.channels;

import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class RedisChannel<T> implements Channel<T> {

    private static final String GRAPHQL_CHANNEL_PREFIX = "graphql:";

    private final ReactiveRedisOperations<Object, Object> redisOperations;
    private final String channelName;

    public RedisChannel(ReactiveRedisOperations<Object, Object> redisOperations, String channelName) {
        this.redisOperations = redisOperations;
        this.channelName = GRAPHQL_CHANNEL_PREFIX + channelName;
    }

    @Override
    public Mono<T> push(T message) {
        return redisOperations.convertAndSend(channelName, message)
                .map(n -> message);
    }

    @Override
    public Flux<T> receive() {
        return redisOperations.listenToChannel(channelName)
                .map(ReactiveSubscription.Message::getMessage)
                .map(m -> (T) m);
    }
}
