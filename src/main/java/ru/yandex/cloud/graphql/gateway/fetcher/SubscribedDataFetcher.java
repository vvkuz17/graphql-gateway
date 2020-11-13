package ru.yandex.cloud.graphql.gateway.fetcher;

import java.util.concurrent.CompletableFuture;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;

import ru.yandex.cloud.graphql.gateway.channels.Channel;

@RequiredArgsConstructor
public class SubscribedDataFetcher<T> implements DataFetcher<CompletableFuture<T>> {

    private final DataFetcher<CompletableFuture<T>> fetcher;
    private final Channel<T> channel;

    @Override
    public CompletableFuture<T> get(DataFetchingEnvironment environment) throws Exception {
        return fetcher.get(environment).thenCompose(t -> channel.push(t).toFuture());
    }
}
