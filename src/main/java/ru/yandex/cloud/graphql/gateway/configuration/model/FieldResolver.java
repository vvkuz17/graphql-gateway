package ru.yandex.cloud.graphql.gateway.configuration.model;

import lombok.Data;

@Data
public class FieldResolver {
    private String type;
    private String field;
    private String datasource;
    private String operation;
    private boolean subscribed;
}
