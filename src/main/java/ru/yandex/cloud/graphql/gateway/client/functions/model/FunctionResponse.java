package ru.yandex.cloud.graphql.gateway.client.functions.model;

import java.util.List;

import lombok.Data;

@Data
public class FunctionResponse<V> {
    V data;
    String errorMessage;
    String errorType;
    List<StackTraceItem> stackTrace;
}
