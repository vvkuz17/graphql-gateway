package ru.yandex.cloud.graphql.gateway.subscriptions;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface Channel<T> {
    Mono<Void> push(T message);

    Flux<T> receive();
}
