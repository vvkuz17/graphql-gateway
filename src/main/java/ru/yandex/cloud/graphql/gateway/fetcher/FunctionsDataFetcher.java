package ru.yandex.cloud.graphql.gateway.fetcher;

import java.util.concurrent.CompletableFuture;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLNamedType;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;

import ru.yandex.cloud.graphql.gateway.client.functions.FunctionsClient;
import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionRequest;
import ru.yandex.cloud.graphql.gateway.configuration.model.DataSource;
import ru.yandex.cloud.graphql.gateway.transformer.FieldTransformer;

@RequiredArgsConstructor
public class FunctionsDataFetcher<T> implements DataFetcher<CompletableFuture<T>> {

    private final DataSource dataSource;
    private final FunctionsClient<T> functionsClient;

    @Override
    public CompletableFuture<T> get(DataFetchingEnvironment environment) {
        FunctionRequest request = FunctionRequest.builder()
                .parentType(((GraphQLNamedType) environment.getParentType()).getName())
                .field(FieldTransformer.transform(environment.getField()))
                .source(environment.getSource())
                // TODO: add selection set
                // TODO: add directives
                // TODO: add local context
                // TODO: add global context?
                // TODO: add cache control?
                .build();

        DataLoader<FunctionRequest, T> loader = environment.getDataLoader(dataSource.toString());
        if (loader != null) {
            return loader.load(request);
        } else {
            return functionsClient.invoke(request).toFuture();
        }
    }
}
