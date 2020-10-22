package ru.yandex.cloud.graphql.gateway.loader;

import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import lombok.RequiredArgsConstructor;
import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;
import org.dataloader.Try;

import ru.yandex.cloud.graphql.gateway.client.functions.FunctionsClient;
import ru.yandex.cloud.graphql.gateway.client.functions.exception.FunctionException;
import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionRequest;
import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionResponse;

@RequiredArgsConstructor
public class FunctionsBatchLoader<V> implements BatchLoaderWithContext<FunctionRequest, Try<V>> {

    private final FunctionsClient<V> functionsClient;

    @Override
    public CompletionStage<List<Try<V>>> load(
            List<FunctionRequest> requests,
            BatchLoaderEnvironment environment
    ) {
        return functionsClient.batchInvoke(requests)
                .flatMapIterable(Function.identity())
                .map(this::toTry)
                .collectList()
                .toFuture();
    }

    private Try<V> toTry(FunctionResponse<V> response) {
        if (response.getErrorMessage() != null) {
            return Try.failed(FunctionException.fromResponse(response));
        } else {
            return Try.succeeded(response.getData());
        }
    }
}
