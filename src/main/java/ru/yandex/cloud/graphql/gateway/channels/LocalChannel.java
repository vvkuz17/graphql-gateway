package ru.yandex.cloud.graphql.gateway.channels;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

public class LocalChannel<T> implements Channel<T> {

    private final Sinks.Many<Object> buffer;

    public LocalChannel() {
        this.buffer = Sinks.many().multicast().onBackpressureBuffer();
    }

    @Override
    public Mono<Void> push(T message) {
        buffer.tryEmitNext(message);
        return Mono.empty();
    }

    @Override
    public Flux<T> receive() {
        return (Flux<T>) buffer.asFlux();
    }
}
