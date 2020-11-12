package ru.yandex.cloud.graphql.gateway.wiring;

import java.util.Map;
import java.util.Optional;

import graphql.schema.GraphQLArgument;
import graphql.schema.GraphQLDirective;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;
import lombok.RequiredArgsConstructor;

import ru.yandex.cloud.graphql.gateway.fetcher.SubscriptionDataFetcher;
import ru.yandex.cloud.graphql.gateway.channels.Channel;
import ru.yandex.cloud.graphql.gateway.channels.ChannelRegistry;

import static graphql.schema.FieldCoordinates.coordinates;

@RequiredArgsConstructor
public class SubscribeDirectiveWiring implements SchemaDirectiveWiring {

    public static final String SUBSCRIBE = "subscribe";
    private static final String MUTATION_ARGUMENT_NAME = "mutation";

    private final ChannelRegistry channelRegistry;

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
        GraphQLFieldDefinition field = environment.getElement();

        Optional<GraphQLDirective> directive = field.getDirectives().stream()
                .filter(d -> SUBSCRIBE.equals(d.getName()))
                .findFirst();

        if (directive.isPresent()) {
            GraphQLArgument mutationArg = directive.get().getArgument(MUTATION_ARGUMENT_NAME);

            if (mutationArg == null) {
                throw new RuntimeException("Argument mutation is not defined for directive subscribe");
            }

            String mutation = (String) mutationArg.getValue();

            Channel<Map<String, Object>> channel = channelRegistry.getChannel(mutation);
            if (channel == null) {
                throw new RuntimeException("Channel is not registered for mutation " + mutation);
            }

            SubscriptionDataFetcher fetcher = new SubscriptionDataFetcher(channel);

            environment.getCodeRegistry().dataFetcher(coordinates(environment.getFieldsContainer(), field), fetcher);
        }

        return field;
    }
}
