package ru.yandex.cloud.graphql.gateway.ws.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class ErrorMessage extends PayloadMessage<List<Map<String, Object>>> {

    @JsonCreator
    public ErrorMessage(
            @JsonProperty("id") String id,
            @JsonProperty("payload") List<Map<String, Object>> errors
    ) {
        super(id, GQL_ERROR, errors);
    }
}
