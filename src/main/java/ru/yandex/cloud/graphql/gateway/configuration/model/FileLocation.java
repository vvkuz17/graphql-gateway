package ru.yandex.cloud.graphql.gateway.configuration.model;

import lombok.Data;

@Data
public class FileLocation {
    private String path;
    private String bucket;
    private String key;
}
