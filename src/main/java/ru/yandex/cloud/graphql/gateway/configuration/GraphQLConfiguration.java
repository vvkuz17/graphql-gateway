package ru.yandex.cloud.graphql.gateway.configuration;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
import graphql.execution.SubscriptionExecutionStrategy;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.ScalarInfo;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ru.yandex.cloud.graphql.gateway.GraphQLExceptionHandler;
import ru.yandex.cloud.graphql.gateway.GraphQLExecutor;
import ru.yandex.cloud.graphql.gateway.coercing.ObjectCoercing;
import ru.yandex.cloud.graphql.gateway.configuration.model.DataSource;
import ru.yandex.cloud.graphql.gateway.configuration.model.FieldResolver;
import ru.yandex.cloud.graphql.gateway.configuration.model.GraphQLApi;
import ru.yandex.cloud.graphql.gateway.fetcher.SubscribedDataFetcher;
import ru.yandex.cloud.graphql.gateway.fetcher.factory.FunctionsDataFetcherFactory;
import ru.yandex.cloud.graphql.gateway.loader.BatchLoaderRegistry;
import ru.yandex.cloud.graphql.gateway.subscriptions.Channel;
import ru.yandex.cloud.graphql.gateway.subscriptions.ChannelRegistry;
import ru.yandex.cloud.graphql.gateway.subscriptions.LocalChannel;
import ru.yandex.cloud.graphql.gateway.util.FileLoader;
import ru.yandex.cloud.graphql.gateway.wiring.SubscribeDirectiveWiring;

@Configuration(proxyBeanMethods = false)
public class GraphQLConfiguration {

    @Bean
    public GraphQLExecutor graphQLExecutor(BatchLoaderRegistry batchLoaderRegistry, GraphQL graphQL) {
        return new GraphQLExecutor(graphQL, batchLoaderRegistry);
    }

    @Bean
    public BatchLoaderRegistry batchLoaderRegistry() {
        return new BatchLoaderRegistry();
    }

    @Bean
    public ChannelRegistry channelRegistry() {
        return new ChannelRegistry();
    }

    @Bean
    public FunctionsDataFetcherFactory functionsDataFetcherFactory(
            BatchLoaderRegistry batchLoaderRegistry,
            @Value("${functions.api.url}") String functionsApiUrl
    ) {
        return new FunctionsDataFetcherFactory(functionsApiUrl, batchLoaderRegistry);
    }

    @Bean
    @SneakyThrows
    public GraphQLApi graphQLApi(GraphqlApiYaml graphqlApiYaml, FileLoader fileLoader) {
        String apiYamlString = fileLoader.readFile(graphqlApiYaml);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        return mapper.readValue(apiYamlString, GraphQLApi.class);
    }

    @Bean
    public GraphQL graphQL(
            GraphQLApi graphqlApi,
            FunctionsDataFetcherFactory functionsDataFetcherFactory,
            FileLoader fileLoader,
            ChannelRegistry channelRegistry
    ) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(fileLoader.readFile(graphqlApi.getSchema()));

        RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();

        runtimeWiring.directive(SubscribeDirectiveWiring.SUBSCRIBE, new SubscribeDirectiveWiring(channelRegistry));

        initDataFetchers(graphqlApi, functionsDataFetcherFactory, runtimeWiring, channelRegistry);
        initScalars(typeRegistry, runtimeWiring);
        initTypeResolvers(graphqlApi, runtimeWiring);

        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring.build());

        GraphQLExceptionHandler exceptionHandler = new GraphQLExceptionHandler();

        return GraphQL.newGraphQL(graphQLSchema)
                .queryExecutionStrategy(new AsyncExecutionStrategy(exceptionHandler))
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(exceptionHandler))
                .subscriptionExecutionStrategy(new SubscriptionExecutionStrategy(exceptionHandler))
                .instrumentation(new TracingInstrumentation())
                .build();
    }

    private void initDataFetchers(
            GraphQLApi graphqlApi,
            FunctionsDataFetcherFactory functionsDataFetcherFactory,
            RuntimeWiring.Builder runtimeWiring,
            ChannelRegistry channelRegistry
    ) {
        Optional.ofNullable(graphqlApi.getFieldResolvers())
                .orElse(Collections.emptyList())
                .stream()
                .collect(Collectors.groupingBy(FieldResolver::getType))
                .forEach((type, resolvers) -> {
                            TypeRuntimeWiring.Builder typeWiring = TypeRuntimeWiring.newTypeWiring(type);
                            resolvers.forEach(fieldResolver -> {
                                        DataSource dataSource = Optional.ofNullable(graphqlApi.getDatasources())
                                                .orElse(Collections.emptyList())
                                                .stream()
                                                .collect(Collectors.toMap(DataSource::getName, Function.identity()))
                                                .get(fieldResolver.getDatasource());

                                        DataFetcher<CompletableFuture<Map<String, Object>>> dataFetcher =
                                                createDataFetcher(dataSource, functionsDataFetcherFactory);

                                        if (fieldResolver.isSubscribed()) {
                                            Channel<Map<String, Object>> channel = new LocalChannel<>();
                                            channelRegistry.register(fieldResolver.getField(), channel);
                                            dataFetcher = new SubscribedDataFetcher<>(dataFetcher, channel);
                                        }

                                        typeWiring.dataFetcher(fieldResolver.getField(), dataFetcher);
                                    }
                            );
                            runtimeWiring.type(typeWiring.build());
                        }
                );
    }

    private void initTypeResolvers(GraphQLApi graphqlApi, RuntimeWiring.Builder runtimeWiring) {
        Optional.ofNullable(graphqlApi.getTypeResolvers())
                .orElse(Collections.emptyList())
                .forEach(typeResolver -> runtimeWiring.type(
                        typeResolver.getType(),
                        builder -> builder.typeResolver(
                                env -> env.getSchema().getObjectType(((Map<String, Object>) env.getObject()).get(typeResolver.getByField()).toString())
                        )
                ));
    }

    private void initScalars(TypeDefinitionRegistry typeRegistry, RuntimeWiring.Builder runtimeWiring) {
        typeRegistry.scalars().forEach((name, definition) -> {
            if (!ScalarInfo.isGraphqlSpecifiedScalar(name)) {
                runtimeWiring.scalar(
                        GraphQLScalarType.newScalar()
                                .name(name)
                                .coercing(new ObjectCoercing())
                                .build());
            }
        });
    }

    private <T> DataFetcher<CompletableFuture<T>> createDataFetcher(
            DataSource dataSource,
            FunctionsDataFetcherFactory functionsDataFetcherFactory) {
        switch (dataSource.getType()) {
            case YandexCloudFunction:
                return functionsDataFetcherFactory.create(dataSource);
        }

        throw new IllegalArgumentException("Unknown datasource: " + dataSource.getType());
    }
}
