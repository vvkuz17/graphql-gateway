package ru.yandex.cloud.graphql.gateway.channels;

import lombok.Value;

@Value
public class Message<T> {
    T payload;
}
