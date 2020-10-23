package ru.yandex.cloud.graphql.gateway.configuration.model;

import lombok.Data;

@Data
public class DataSource {
    private String name;
    private Type type;
    private String resource;
    private boolean batched;

    public enum Type {
        YandexCloudFunction
    }
}
