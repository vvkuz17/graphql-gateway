package ru.yandex.cloud.graphql.gateway.transformer.error;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import graphql.language.SourceLocation;
import org.springframework.util.StringUtils;

import ru.yandex.cloud.graphql.gateway.client.functions.model.StackTraceItem;
import ru.yandex.cloud.graphql.gateway.client.functions.exception.FunctionException;
import ru.yandex.cloud.graphql.gateway.model.GraphQLApiError;

public class FunctionExceptionTransformer implements ErrorTransformer {
    @Override
    public GraphQLApiError.GraphQLApiErrorBuilder transform(Throwable e) {
        if (e instanceof FunctionException) {
            FunctionException fe = (FunctionException) e;
            return GraphQLApiError.builder()
                    .message(e.getMessage())
                    .errorType(GraphQLApiError.Type.FunctionError)
                    .locations(transform(fe.getFunctionStackTrace()))
                    .extensions(Collections.singletonMap("errorType", fe.getType()));
        }
        return null;
    }

    private List<SourceLocation> transform(List<StackTraceItem> stackTrace) {
        return stackTrace == null ? Collections.emptyList() :
                stackTrace.stream()
                        .filter(item -> !StringUtils.isEmpty(item.getFile()) && item.getLine() != null && item.getColumn() != null)
                        .map(item -> new SourceLocation(item.getLine(), item.getColumn(), item.getFile()))
                        .collect(Collectors.toList());
    }
}
