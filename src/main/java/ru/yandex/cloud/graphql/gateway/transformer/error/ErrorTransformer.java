package ru.yandex.cloud.graphql.gateway.transformer.error;

import ru.yandex.cloud.graphql.gateway.model.GraphQLApiError;

public interface ErrorTransformer<E> {
    GraphQLApiError.GraphQLApiErrorBuilder transform(E throwable);
}
