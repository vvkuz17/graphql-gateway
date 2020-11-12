package ru.yandex.cloud.graphql.gateway.fetcher;

import java.util.Map;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;

import ru.yandex.cloud.graphql.gateway.subscriptions.Channel;

@RequiredArgsConstructor
public class SubscriptionDataFetcher implements DataFetcher<Publisher<Map<String, Object>>> {

    private final Channel<Map<String, Object>> channel;

    @Override
    public Publisher<Map<String, Object>> get(DataFetchingEnvironment environment) {
        return channel.receive()
                .filter(map -> environment.getArguments().entrySet().stream()
                        .allMatch(e -> e.getValue().equals(map.get(e.getKey())))
                );
    }
}
