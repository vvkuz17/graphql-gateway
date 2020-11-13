package ru.yandex.cloud.graphql.gateway.configuration.model;

import lombok.Data;

@Data
public class Subscriptions {
    private Mode mode;

    public enum Mode {
        local, redis
    }
}
