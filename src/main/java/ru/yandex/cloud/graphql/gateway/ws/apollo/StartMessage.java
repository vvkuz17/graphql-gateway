package ru.yandex.cloud.graphql.gateway.ws.apollo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;

public class StartMessage extends PayloadMessage<GraphQLRequest> {

    @JsonCreator
    public StartMessage(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("payload") GraphQLRequest payload
    ) {
        super(id, GQL_START, payload);
    }
}
