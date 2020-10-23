package ru.yandex.cloud.graphql.gateway.configuration;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.AsyncSerialExecutionStrategy;
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
import ru.yandex.cloud.graphql.gateway.coercing.ObjectCoercing;
import ru.yandex.cloud.graphql.gateway.configuration.model.DataSource;
import ru.yandex.cloud.graphql.gateway.configuration.model.FieldResolver;
import ru.yandex.cloud.graphql.gateway.configuration.model.GraphQLApi;
import ru.yandex.cloud.graphql.gateway.fetcher.factory.FunctionsDataFetcherFactory;
import ru.yandex.cloud.graphql.gateway.registry.BatchLoaderRegistry;
import ru.yandex.cloud.graphql.gateway.util.FileLoader;

@Configuration(proxyBeanMethods = false)
public class GraphQLConfiguration {

    private final FileLoader fileLoader;

    public GraphQLConfiguration(FileLoader fileLoader) {
        this.fileLoader = fileLoader;
    }

    @Bean
    public BatchLoaderRegistry batchLoaderRegistry() {
        return new BatchLoaderRegistry();
    }

    @Bean
    public FunctionsDataFetcherFactory functionsDataFetcherFactory(
            BatchLoaderRegistry batchLoaderRegistry,
            @Value("${functions.api.url}") String functionsApiUrl
    ) {
        return new FunctionsDataFetcherFactory(functionsApiUrl, batchLoaderRegistry);
    }

    @Bean
    public GraphQLExceptionHandler graphQLExceptionHandler() {
        return new GraphQLExceptionHandler();
    }

    @Bean
    @SneakyThrows
    public GraphQLApi graphQLApi(GraphqlApiYaml graphqlApiYaml) {
        String apiYamlString = fileLoader.readFile(graphqlApiYaml);
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        return mapper.readValue(apiYamlString, GraphQLApi.class);
    }

    @Bean
    public GraphQL graphQL(
            GraphQLApi graphqlApi,
            GraphQLExceptionHandler exceptionHandler,
            FunctionsDataFetcherFactory functionsDataFetcherFactory
    ) {
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(fileLoader.readFile(graphqlApi.getSchema()));

        RuntimeWiring.Builder runtimeWiring = RuntimeWiring.newRuntimeWiring();

        initDataFetchers(graphqlApi, functionsDataFetcherFactory, runtimeWiring);
        initScalars(typeRegistry, runtimeWiring);
        initTypeResolvers(graphqlApi, runtimeWiring);

        GraphQLSchema graphQLSchema = new SchemaGenerator().makeExecutableSchema(typeRegistry, runtimeWiring.build());

        return GraphQL.newGraphQL(graphQLSchema)
                .queryExecutionStrategy(new AsyncExecutionStrategy(exceptionHandler))
                .mutationExecutionStrategy(new AsyncSerialExecutionStrategy(exceptionHandler))
                .instrumentation(new TracingInstrumentation())
                .build();
    }

    private void initDataFetchers(
            GraphQLApi graphqlApi,
            FunctionsDataFetcherFactory functionsDataFetcherFactory,
            RuntimeWiring.Builder runtimeWiring
    ) {
        graphqlApi.getFieldResolvers().stream()
                .collect(Collectors.groupingBy(FieldResolver::getType))
                .forEach((type, resolvers) -> {
                            TypeRuntimeWiring.Builder typeWiring = TypeRuntimeWiring.newTypeWiring(type);
                            resolvers.forEach(fieldResolver -> {
                                        DataSource dataSource = graphqlApi.getDatasources().stream()
                                                .collect(Collectors.toMap(DataSource::getName, Function.identity()))
                                                .get(fieldResolver.getDatasource());
                                        typeWiring.dataFetcher(
                                                fieldResolver.getField(),
                                                createDataFetcher(dataSource, functionsDataFetcherFactory));
                                    }
                            );
                            runtimeWiring.type(typeWiring.build());
                        }
                );
    }

    private void initTypeResolvers(GraphQLApi graphqlApi, RuntimeWiring.Builder runtimeWiring) {
        graphqlApi.getTypeResolvers()
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
