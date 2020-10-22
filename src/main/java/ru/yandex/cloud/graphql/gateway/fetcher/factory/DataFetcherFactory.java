package ru.yandex.cloud.graphql.gateway.fetcher.factory;

import java.util.concurrent.CompletableFuture;

import graphql.schema.DataFetcher;

import ru.yandex.cloud.graphql.gateway.configuration.model.DataSource;

public interface DataFetcherFactory {
    <T> DataFetcher<CompletableFuture<T>> create(DataSource dataSource);
}
