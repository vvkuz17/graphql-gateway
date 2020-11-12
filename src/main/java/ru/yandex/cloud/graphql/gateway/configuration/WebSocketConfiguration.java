package ru.yandex.cloud.graphql.gateway.configuration;

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import ru.yandex.cloud.graphql.gateway.GraphQLExecutor;
import ru.yandex.cloud.graphql.gateway.ws.GraphQLWebSocketHandler;

@Configuration
public class WebSocketConfiguration {

    @Bean
    public HandlerMapping handlerMapping(
            @Value("${graphql.ws.endpoint:/graphql}") String webSocketEndpoint,
            WebSocketHandler webSocketHandler
    ) {
        Map<String, WebSocketHandler> map = Collections.singletonMap(webSocketEndpoint, webSocketHandler);
        return new SimpleUrlHandlerMapping(map, 1);
    }

    @Bean
    public WebSocketHandler webSocketHandler(GraphQLExecutor graphQLExecutor) {
        return new GraphQLWebSocketHandler(graphQLExecutor);
    }

    @Bean
    public HandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
