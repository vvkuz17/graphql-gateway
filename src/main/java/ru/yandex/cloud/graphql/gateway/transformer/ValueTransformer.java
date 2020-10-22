package ru.yandex.cloud.graphql.gateway.transformer;

import java.util.stream.Collectors;

import graphql.language.ArrayValue;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.Value;

public class ValueTransformer {
    public static Object transform(Value value) {
        if (value instanceof BooleanValue) {
            return ((BooleanValue) value).isValue();
        } else if (value instanceof FloatValue) {
            return ((FloatValue) value).getValue();
        } else if (value instanceof IntValue) {
            return ((IntValue) value).getValue();
        } else if (value instanceof StringValue) {
            return ((StringValue) value).getValue();
        } else if (value instanceof EnumValue) {
            return ((EnumValue) value).getName();
        } else if (value instanceof NullValue) {
            return null;
        } else if (value instanceof ObjectValue) {
            return ((ObjectValue) value).getObjectFields().stream()
                    .collect(Collectors.toMap(
                            ObjectField::getName,
                            field -> ValueTransformer.transform(field.getValue())
                    ));
        } else if (value instanceof ArrayValue) {
            return ((ArrayValue) value).getValues().stream()
                    .map(ValueTransformer::transform)
                    .collect(Collectors.toList());
        }
        throw new IllegalArgumentException("Unknown value type: " + value.getClass().getSimpleName());
    }
}
