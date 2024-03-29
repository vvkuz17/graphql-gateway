package ru.yandex.cloud.graphql.gateway.channels;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Channel<T> {
    Mono<T> push(T message);

    Flux<T> receive();
}
