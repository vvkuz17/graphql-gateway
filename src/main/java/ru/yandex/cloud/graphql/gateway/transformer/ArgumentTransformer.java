package ru.yandex.cloud.graphql.gateway.transformer;

import ru.yandex.cloud.graphql.gateway.client.functions.model.Argument;

public class ArgumentTransformer {
    public static Argument transform(graphql.language.Argument argument) {
        return new Argument(argument.getName(), ValueTransformer.transform(argument.getValue()));
    }
}
