package ru.yandex.cloud.graphql.gateway.client.functions.model;

import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FunctionRequest {
    String parentType;
    Field field;
    Map<String, Object> source;
}
