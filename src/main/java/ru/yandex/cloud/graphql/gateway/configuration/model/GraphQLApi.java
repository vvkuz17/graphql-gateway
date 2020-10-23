package ru.yandex.cloud.graphql.gateway.configuration.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class GraphQLApi {
    private String name;
    private FileLocation schema;
    private List<DataSource> datasources = new ArrayList<>();
    private List<FieldResolver> fieldResolvers = new ArrayList<>();
    private List<TypeResolver> typeResolvers = new ArrayList<>();
}
