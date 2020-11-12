package ru.yandex.cloud.graphql.gateway.ws;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import graphql.ExecutionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import ru.yandex.cloud.graphql.gateway.GraphQLExecutor;
import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;
import ru.yandex.cloud.graphql.gateway.ws.apollo.ApolloMessage;
import ru.yandex.cloud.graphql.gateway.ws.apollo.StartMessage;

import static ru.yandex.cloud.graphql.gateway.ws.apollo.ApolloMessage.GQL_CONNECTION_INIT;
import static ru.yandex.cloud.graphql.gateway.ws.apollo.ApolloMessage.GQL_CONNECTION_TERMINATE;
import static ru.yandex.cloud.graphql.gateway.ws.apollo.ApolloMessage.GQL_START;
import static ru.yandex.cloud.graphql.gateway.ws.apollo.ApolloMessage.GQL_STOP;

@Slf4j
@RequiredArgsConstructor
public class GraphQLWebSocketHandler implements WebSocketHandler {
    private static final List<String> GRAPHQL_WS = Collections.singletonList("graphql-ws");

    private final GraphQLExecutor graphQLExecutor;

    @NonNull
    @Override
    public List<String> getSubProtocols() {
        return GRAPHQL_WS;
    }

    @NonNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
                session.receive()
                        .flatMap(message -> handleMessage(message, session))
                        .map(session::textMessage)
        );
    }

    private Publisher<? extends String> handleMessage(WebSocketMessage message, WebSocketSession session) {
        try {
            ApolloMessage apolloMessage = ApolloMessage.from(message.getPayloadAsText());

            switch (apolloMessage.getType()) {
                case GQL_CONNECTION_INIT:
                    return Mono.just(ApolloMessage.connectionAck());
                case GQL_START:
                    return executeGraphQLRequest(apolloMessage.getId(), ((StartMessage) apolloMessage).getPayload());
                case GQL_CONNECTION_TERMINATE:
                    session.close();
                case GQL_STOP:
                default:
                    return Mono.empty();
            }
        } catch (Exception e) {
            return connectionError(e);
        }
    }

    private Flux<String> executeGraphQLRequest(String requestId, GraphQLRequest request) throws JsonProcessingException {
        ExecutionResult result = graphQLExecutor.execute(request);

        if (result.getData() instanceof Publisher) {
            return Flux.from(result.getData())
                    .cast(ExecutionResult.class)
                    .flatMap(executionResult -> transform(requestId, executionResult));
        } else {
            return Flux.just(
                    ApolloMessage.data(requestId, result.toSpecification()),
                    ApolloMessage.complete(requestId)
            );
        }
    }

    private static Publisher<? extends String> transform(String requestId, ExecutionResult executionResult) {
        try {
            if (executionResult.getErrors().isEmpty()) {
                return Mono.just(ApolloMessage.data(requestId, executionResult.toSpecification()));
            } else {
                return Mono.just(ApolloMessage.error(requestId, executionResult.getErrors()));
            }
        } catch (JsonProcessingException e) {
            return connectionError(e);
        }
    }

    private static Publisher<? extends String> connectionError(Throwable e) {
        try {
            log.error("Failed to handle graphQL websocket request: ", e);
            return Mono.just(ApolloMessage.connectionError());
        } catch (JsonProcessingException ignored) {
            return Mono.error(ignored);
        }
    }
}
