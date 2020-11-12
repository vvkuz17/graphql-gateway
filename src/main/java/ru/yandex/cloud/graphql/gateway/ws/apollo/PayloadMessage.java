package ru.yandex.cloud.graphql.gateway.ws.apollo;

abstract class PayloadMessage<T> extends ApolloMessage {

    private final T payload;

    PayloadMessage(String id, String type, T payload) {
        super(id, type);
        this.payload = payload;
    }

    public T getPayload() {
        return payload;
    }
}
