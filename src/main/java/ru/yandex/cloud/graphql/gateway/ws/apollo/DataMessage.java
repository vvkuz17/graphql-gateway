package ru.yandex.cloud.graphql.gateway.ws.apollo;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DataMessage extends PayloadMessage<Map<String, Object>> {

    @JsonCreator
    public DataMessage(
            @JsonProperty("id") String id,
            @JsonProperty("payload") Map<String, Object> payload
    ) {
        super(id, GQL_DATA, payload);
    }
}
