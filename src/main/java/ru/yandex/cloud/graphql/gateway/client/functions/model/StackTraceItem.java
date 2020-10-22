package ru.yandex.cloud.graphql.gateway.client.functions.model;

import lombok.Data;

@Data
public class StackTraceItem {
    String function;
    String file;
    Integer line;
    Integer column;
}
