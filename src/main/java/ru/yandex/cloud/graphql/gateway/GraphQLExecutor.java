package ru.yandex.cloud.graphql.gateway;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.springframework.stereotype.Component;

import ru.yandex.cloud.graphql.gateway.model.GraphQLContext;
import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;
import ru.yandex.cloud.graphql.gateway.registry.BatchLoaderRegistry;

@Component
@RequiredArgsConstructor
public class GraphQLExecutor {

    private final GraphQL graphQL;
    private final BatchLoaderRegistry batchLoaderRegistry;

    public CompletableFuture<Map<String, Object>> execute(GraphQLRequest graphQLRequest) {
        GraphQLContext context = new GraphQLContext(graphQLRequest);

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(graphQLRequest.getQuery())
                .operationName(graphQLRequest.getOperationName())
                .variables(graphQLRequest.getVariables())
                .context(context)
                .dataLoaderRegistry(createDataLoaderRegistry(context))
                .build();

        return graphQL.executeAsync(executionInput).thenApply(ExecutionResult::toSpecification);
    }

    private DataLoaderRegistry createDataLoaderRegistry(GraphQLContext context) {
        DataLoaderRegistry registry = new DataLoaderRegistry();

        DataLoaderOptions options = DataLoaderOptions.newOptions()
                .setBatchLoaderContextProvider(() -> context);

        batchLoaderRegistry.getLoaders().forEach(
                (key, loader) -> registry.register(
                        key.toString(),
                        DataLoader.newDataLoaderWithTry(loader, options)
                )
        );
        return registry;
    }
}
