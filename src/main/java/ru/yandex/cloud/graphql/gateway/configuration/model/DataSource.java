package ru.yandex.cloud.graphql.gateway.configuration.model;

import lombok.Data;

@Data
public class DataSource {
    String name;
    Type type;
    String resource;
    boolean batched;

    public enum Type {
        YandexCloudFunction
    }
}
