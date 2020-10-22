package ru.yandex.cloud.graphql.gateway.model;

import java.util.List;
import java.util.Map;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import lombok.Builder;
import lombok.Value;

import ru.yandex.cloud.graphql.gateway.transformer.error.GraphqlErrorHelper;

@Value
@Builder
public class GraphQLApiError implements GraphQLError {
    ErrorClassification errorType;
    String message;
    List<Object> path;
    List<SourceLocation> locations;
    Map<String, Object> extensions;

    @Override
    public Map<String, Object> toSpecification() {
        return GraphqlErrorHelper.toSpecification(this);
    }

    public enum Type implements ErrorClassification {
        InternalGatewayError, FunctionError
    }
}
