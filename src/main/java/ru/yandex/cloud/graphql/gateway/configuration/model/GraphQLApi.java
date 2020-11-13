package ru.yandex.cloud.graphql.gateway.configuration.model;

import java.util.List;

import lombok.Data;

@Data
public class GraphQLApi {
    private String name;
    private FileLocation schema;
    private Subscriptions subscriptions;
    private List<DataSource> datasources;
    private List<FieldResolver> fieldResolvers;
    private List<TypeResolver> typeResolvers;
}
