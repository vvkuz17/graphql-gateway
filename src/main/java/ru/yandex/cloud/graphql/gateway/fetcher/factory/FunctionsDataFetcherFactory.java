package ru.yandex.cloud.graphql.gateway.fetcher.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import graphql.schema.DataFetcher;

import ru.yandex.cloud.graphql.gateway.client.functions.FunctionsClient;
import ru.yandex.cloud.graphql.gateway.configuration.model.DataSource;
import ru.yandex.cloud.graphql.gateway.fetcher.FunctionsDataFetcher;
import ru.yandex.cloud.graphql.gateway.loader.FunctionsBatchLoader;
import ru.yandex.cloud.graphql.gateway.registry.BatchLoaderRegistry;

public class FunctionsDataFetcherFactory implements DataFetcherFactory {
    private final String functionsApiUrl;
    private final BatchLoaderRegistry batchLoaderRegistry;
    private final Map<String, FunctionsClient> functionsClients;

    public FunctionsDataFetcherFactory(String functionsApiUrl, BatchLoaderRegistry batchLoaderRegistry) {
        this.functionsApiUrl = functionsApiUrl;
        this.batchLoaderRegistry = batchLoaderRegistry;
        this.functionsClients = new HashMap<>();
    }

    @Override
    public <T> DataFetcher<CompletableFuture<T>> create(DataSource dataSource) {
        FunctionsClient<T> functionsClient = functionsClients.getOrDefault(
                dataSource.getName(),
                new FunctionsClient<>(functionsApiUrl, dataSource.getResource())
        );

        if (!functionsClients.containsKey(dataSource.getName())) {
            functionsClients.put(dataSource.getName(), functionsClient);
            if (dataSource.isBatched()) {
                batchLoaderRegistry.register(dataSource.toString(), new FunctionsBatchLoader<>(functionsClient));
            }
        }

        return new FunctionsDataFetcher<>(dataSource, functionsClient);
    }
}
