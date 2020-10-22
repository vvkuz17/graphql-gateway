package ru.yandex.cloud.graphql.gateway.client.functions.model;

import java.util.List;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Field {
    String name;
    String alias;
    List<Argument> arguments;
}
