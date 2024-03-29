package ru.yandex.cloud.graphql.gateway;

import java.util.Map;
import java.util.Optional;

import graphql.ExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;

@CrossOrigin
@RestController
@RequiredArgsConstructor
public class GraphQLController {

    private final GraphQLExecutor executor;

    @PostMapping(
            value = "${graphql.http.endpoint:/graphql}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<Map<String, Object>> executeJsonPost(
            @RequestBody GraphQLRequest requestBody,
            GraphQLRequest request
    ) {
        request = new GraphQLRequest(
                Optional.ofNullable(request.getQuery()).orElse(requestBody.getQuery()),
                Optional.ofNullable(request.getOperationName()).orElse(requestBody.getOperationName()),
                Optional.ofNullable(request.getVariables()).orElse(requestBody.getVariables()),
                Optional.ofNullable(request.getExtensions()).orElse(requestBody.getVariables())
        );

        return Mono.fromFuture(executor.executeAsync(request))
                .map(ExecutionResult::toSpecification);
    }

    @PostMapping(
            value = "${graphql.http.endpoint:/graphql}",
            consumes = {"application/graphql", "application/graphql;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<Map<String, Object>> executeGraphQLPost(
            @RequestBody String queryBody,
            GraphQLRequest request
    ) {
        request = new GraphQLRequest(
                request.getQuery() == null ? queryBody : request.getQuery(),
                request.getOperationName(),
                request.getVariables(),
                request.getExtensions()
        );
        return Mono.fromFuture(executor.executeAsync(request))
                .map(ExecutionResult::toSpecification);
    }

    @PostMapping(
            value = "${graphql.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Mono<Map<String, Object>> executeFormPost(
            @RequestParam Map<String, String> queryParams,
            GraphQLRequest request
    ) {
        String query = queryParams.get("query");
        String operationName = queryParams.get("operationName");

        request = new GraphQLRequest(
                StringUtils.isEmpty(query) ? request.getQuery() : query,
                StringUtils.isEmpty(operationName) ? request.getOperationName() : operationName,
                request.getVariables(),
                request.getExtensions()
        );

        return Mono.fromFuture(executor.executeAsync(request))
                .map(ExecutionResult::toSpecification);
    }

    @GetMapping(
            value = "${graphql.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            headers = "Connection!=Upgrade"
    )
    public Mono<Map<String, Object>> executeGet(GraphQLRequest graphQLRequest, ServerWebExchange request) {
        return Mono.fromFuture(executor.executeAsync(graphQLRequest))
                .map(ExecutionResult::toSpecification);
    }
}
