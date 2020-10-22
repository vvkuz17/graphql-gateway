package ru.yandex.cloud.graphql.gateway.configuration.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GraphQLApi {
    String name;
    String schema;
    List<DataSource> datasources = new ArrayList<>();
    List<FieldResolver> fieldResolvers = new ArrayList<>();
    List<TypeResolver> typeResolvers = new ArrayList<>();
}
