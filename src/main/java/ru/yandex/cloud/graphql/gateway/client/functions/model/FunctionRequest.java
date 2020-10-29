package ru.yandex.cloud.graphql.gateway.client.functions.model;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FunctionRequest {
    private String parentType;
    private Field field;
    private Map<String, Object> source;
}
