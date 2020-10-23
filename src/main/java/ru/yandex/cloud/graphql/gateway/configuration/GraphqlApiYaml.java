package ru.yandex.cloud.graphql.gateway.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import ru.yandex.cloud.graphql.gateway.configuration.model.FileLocation;

@Configuration
@ConfigurationProperties(prefix = "graphql.api.yml")
public class GraphqlApiYaml extends FileLocation {
}
