package ru.yandex.cloud.graphql.gateway.ws.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ConnectionErrorMessage extends PayloadMessage<Map<String, Object>> {

    @JsonCreator
    public ConnectionErrorMessage(@JsonProperty("payload") Map<String, Object> error) {
        super(null, GQL_CONNECTION_ERROR, error);
    }
}
