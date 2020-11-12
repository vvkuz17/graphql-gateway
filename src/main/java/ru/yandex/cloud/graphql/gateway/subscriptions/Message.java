package ru.yandex.cloud.graphql.gateway.subscriptions;

import lombok.Value;

@Value
public class Message<T> {
    T payload;
}
