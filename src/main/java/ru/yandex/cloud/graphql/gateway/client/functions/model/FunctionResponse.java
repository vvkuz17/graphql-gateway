package ru.yandex.cloud.graphql.gateway.client.functions.model;

import java.util.List;

import lombok.Data;

@Data
public class FunctionResponse<V> {
    private V data;
    private String errorMessage;
    private String errorType;
    private List<StackTraceItem> stackTrace;
}
