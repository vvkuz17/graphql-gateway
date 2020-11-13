package ru.yandex.cloud.graphql.gateway.transformer.error;

import org.springframework.data.redis.RedisConnectionFailureException;

import ru.yandex.cloud.graphql.gateway.model.GraphQLApiError;

public class RedisConnectionFailureExceptionTransformer implements ErrorTransformer {
    @Override
    public GraphQLApiError.GraphQLApiErrorBuilder transform(Throwable e) {
        if (e instanceof RedisConnectionFailureException) {
            return GraphQLApiError.builder()
                    .message(e.getMessage())
                    .errorType(GraphQLApiError.Type.RedisConnectionError);
        }
        return null;
    }
}
