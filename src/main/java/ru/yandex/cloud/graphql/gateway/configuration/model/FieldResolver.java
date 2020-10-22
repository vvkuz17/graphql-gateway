package ru.yandex.cloud.graphql.gateway.configuration.model;

import lombok.Data;

@Data
public class FieldResolver {
    String type;
    String field;
    String datasource;
    String operation;
}
