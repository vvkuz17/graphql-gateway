package ru.yandex.cloud.graphql.gateway;

import java.util.concurrent.CompletableFuture;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import lombok.RequiredArgsConstructor;
import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;
import org.dataloader.DataLoaderRegistry;
import org.springframework.lang.NonNull;

import ru.yandex.cloud.graphql.gateway.loader.BatchLoaderRegistry;
import ru.yandex.cloud.graphql.gateway.model.GraphQLContext;
import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;

@RequiredArgsConstructor
public class GraphQLExecutor {

    private final GraphQL graphQL;
    private final BatchLoaderRegistry batchLoaderRegistry;

    @NonNull
    public ExecutionResult execute(@NonNull GraphQLRequest graphQLRequest) {
        ExecutionInput executionInput = buildExecutionInput(graphQLRequest);
        return graphQL.execute(executionInput);
    }

    @NonNull
    public CompletableFuture<ExecutionResult> executeAsync(@NonNull GraphQLRequest graphQLRequest) {
        ExecutionInput executionInput = buildExecutionInput(graphQLRequest);
        return graphQL.executeAsync(executionInput);
    }

    private ExecutionInput buildExecutionInput(GraphQLRequest graphQLRequest) {
        GraphQLContext context = new GraphQLContext(graphQLRequest);

        return ExecutionInput.newExecutionInput()
                .query(graphQLRequest.getQuery())
                .operationName(graphQLRequest.getOperationName())
                .variables(graphQLRequest.getVariables())
                .context(context)
                .dataLoaderRegistry(createDataLoaderRegistry(context))
                .build();
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
