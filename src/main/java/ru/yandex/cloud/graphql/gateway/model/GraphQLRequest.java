package ru.yandex.cloud.graphql.gateway.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraphQLRequest {
    String query;
    String operationName;
    Map<String, Object> variables;

    public GraphQLRequest(
            @JsonProperty("query") String query,
            @JsonProperty("operationName") String operationName,
            @JsonProperty("variables") Map<String, Object> variables
    ) {
        this.query = query;
        this.operationName = operationName;
        this.variables = variables;
    }
}
