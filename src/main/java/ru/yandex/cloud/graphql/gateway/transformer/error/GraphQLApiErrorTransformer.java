package ru.yandex.cloud.graphql.gateway.transformer.error;

import java.util.Collections;
import java.util.List;

import ru.yandex.cloud.graphql.gateway.model.GraphQLApiError;

public class GraphQLApiErrorTransformer {
    private static final List<ErrorTransformer> errorTransformersChain = Collections.singletonList(
            new FunctionExceptionTransformer()
    );

    public static GraphQLApiError transform(Throwable ex, List<Object> path) {
        for (ErrorTransformer transformer : errorTransformersChain) {
            GraphQLApiError.GraphQLApiErrorBuilder errorBuilder = transformer.transform(ex);
            if (errorBuilder != null) {
                return errorBuilder
                        .path(path)
                        .build();
            }
        }

        return createInternalServerError(path);
    }

    private static GraphQLApiError createInternalServerError(List<Object> path) {
        return GraphQLApiError
                .builder()
                .errorType(GraphQLApiError.Type.InternalGatewayError)
                .message("Internal gateway error")
                .path(path)
                .build();
    }
}
