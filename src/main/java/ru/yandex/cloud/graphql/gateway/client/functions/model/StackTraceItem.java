package ru.yandex.cloud.graphql.gateway.client.functions.model;

import lombok.Data;

@Data
public class StackTraceItem {
    private String function;
    private String file;
    private Integer line;
    private Integer column;
}
