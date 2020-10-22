package ru.yandex.cloud.graphql.gateway.model;

import lombok.Value;

@Value
public class GraphQLContext {
    GraphQLRequest graphQLRequest;
}
