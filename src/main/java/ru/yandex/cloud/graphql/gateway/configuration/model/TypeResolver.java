package ru.yandex.cloud.graphql.gateway.configuration.model;

import lombok.Data;

@Data
public class TypeResolver {
    String type;
    String byField;
}
