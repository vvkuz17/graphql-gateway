package ru.yandex.cloud.graphql.gateway.client.functions.exception;

import java.util.List;
import java.util.Optional;

import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionResponse;
import ru.yandex.cloud.graphql.gateway.client.functions.model.StackTraceItem;

public class FunctionException extends RuntimeException {
    String type;
    List<StackTraceItem> stackTrace;

    public FunctionException(String message, String type, List<StackTraceItem> stackTrace) {
        super(message);
        this.type = type;
        this.stackTrace = stackTrace;
    }

    public String getType() {
        return type;
    }

    public List<StackTraceItem> getFunctionStackTrace() {
        return stackTrace;
    }

    public static <V> FunctionException fromResponse(FunctionResponse<V> response) {
        return Optional.ofNullable(response)
                .map(e -> new FunctionException(
                        e.getErrorMessage(),
                        e.getErrorType(),
                        e.getStackTrace()))
                .orElse(null);
    }
}
