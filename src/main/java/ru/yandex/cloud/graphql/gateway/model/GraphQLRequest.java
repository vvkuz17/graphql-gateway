package ru.yandex.cloud.graphql.gateway.model;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GraphQLRequest {
    private String query;
    private String operationName;
    private Map<String, Object> variables;
}
