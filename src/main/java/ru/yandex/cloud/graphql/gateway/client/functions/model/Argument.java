package ru.yandex.cloud.graphql.gateway.client.functions.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Argument {
    private String name;
    private Object value;
}
