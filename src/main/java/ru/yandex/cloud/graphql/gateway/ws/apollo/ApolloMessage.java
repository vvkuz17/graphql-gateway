package ru.yandex.cloud.graphql.gateway.ws.apollo;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ErrorType;
import graphql.GraphQLError;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, include =
        JsonTypeInfo.As.EXISTING_PROPERTY)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitMessage.class, name = ApolloMessage.GQL_CONNECTION_INIT),
        @JsonSubTypes.Type(value = ApolloMessage.class, name = ApolloMessage.GQL_CONNECTION_TERMINATE),
        @JsonSubTypes.Type(value = ApolloMessage.class, name = ApolloMessage.GQL_STOP),
        @JsonSubTypes.Type(value = StartMessage.class, name = ApolloMessage.GQL_START)
})
@SuppressWarnings("WeakerAccess")
public class ApolloMessage {

    private final String id;
    private final String type;

    //Client messages
    public static final String GQL_CONNECTION_INIT = "connection_init";
    public static final String GQL_CONNECTION_TERMINATE = "connection_terminate";
    public static final String GQL_START = "start";
    public static final String GQL_STOP = "stop";

    //Server messages
    public static final String GQL_CONNECTION_ACK = "connection_ack";
    public static final String GQL_CONNECTION_ERROR = "connection_error";
    public static final String GQL_CONNECTION_KEEP_ALIVE = "ka";
    public static final String GQL_DATA = "data";
    public static final String GQL_ERROR = "error";
    public static final String GQL_COMPLETE = "complete";

    private static final ApolloMessage CONNECTION_ACK = new ApolloMessage(GQL_CONNECTION_ACK);
    private static final ApolloMessage KEEP_ALIVE = new ApolloMessage(GQL_CONNECTION_KEEP_ALIVE);

    private static final ObjectMapper mapper =
            new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private ApolloMessage(String type) {
        this(null, type);
    }

    @JsonCreator
    public ApolloMessage(@JsonProperty("id") String id, @JsonProperty("type") String type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public static ApolloMessage from(String message) throws JsonProcessingException {
        return mapper.readValue(message, ApolloMessage.class);
    }

    public static String connectionAck() throws JsonProcessingException {
        return jsonMessage(CONNECTION_ACK);
    }

    public static String keepAlive() throws JsonProcessingException {
        return jsonMessage(KEEP_ALIVE);
    }

    public static String connectionError() throws JsonProcessingException {
        return jsonMessage(new ConnectionErrorMessage(Collections.singletonMap("message", "Invalid message")));
    }

    public static String data(String id, Map<String, Object> result) throws JsonProcessingException {
        return jsonMessage(new DataMessage(id, result));
    }

    public static String complete(String id) throws JsonProcessingException {
        return jsonMessage(new ApolloMessage(id, GQL_COMPLETE));
    }

    public static String error(String id, List<GraphQLError> errors) throws JsonProcessingException {
        return jsonMessage(new ErrorMessage(id, errors.stream()
                .filter(error -> !error.getErrorType().equals(ErrorType.DataFetchingException))
                .map(GraphQLError::toSpecification)
                .collect(Collectors.toList())));
    }

    public static String error(String id, Throwable exception) throws JsonProcessingException {
        return jsonMessage(new ErrorMessage(id, Collections.singletonList(Collections.singletonMap("message",
                exception.getMessage()))));
    }

    private static String jsonMessage(ApolloMessage message) throws JsonProcessingException {
        return mapper.writeValueAsString(message);
    }
}
