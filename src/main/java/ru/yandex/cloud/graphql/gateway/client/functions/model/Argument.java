package ru.yandex.cloud.graphql.gateway.client.functions.model;

import lombok.Value;

@Value
public class Argument {
    String name;
    Object value;
}
