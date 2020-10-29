package ru.yandex.cloud.graphql.gateway.client.functions.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Field {
    private String name;
    private String alias;
    private List<Argument> arguments;
}
