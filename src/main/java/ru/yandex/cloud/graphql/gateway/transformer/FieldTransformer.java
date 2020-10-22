package ru.yandex.cloud.graphql.gateway.transformer;

import java.util.stream.Collectors;

import ru.yandex.cloud.graphql.gateway.client.functions.model.Field;

public class FieldTransformer {

    public static Field transform(graphql.language.Field field) {
        return Field.builder()
                .name(field.getName())
                .alias(field.getAlias())
                .arguments(field.getArguments().stream()
                        .map(ArgumentTransformer::transform)
                        .collect(Collectors.toList()))
                .build();
    }
}
