package ru.yandex.cloud.graphql.gateway;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class GraphQLController {

    private final GraphQLExecutor executor;

    @PostMapping(
            value = "${graphql.http.endpoint:/graphql}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Mono<Map<String, Object>> executeJsonPost(
            @RequestBody GraphQLRequest requestBody,
            GraphQLRequest requestParams,
            ServerWebExchange request
    ) {
        String query = requestParams.getQuery() == null ?
                requestBody.getQuery() : requestParams.getQuery();
        String operationName = requestParams.getOperationName() == null ?
                requestBody.getOperationName() : requestParams.getOperationName();
        Map<String, Object> variables = requestParams.getVariables() == null ?
                requestBody.getVariables() : requestParams.getVariables();

        GraphQLRequest graphQLRequest = new GraphQLRequest(query, operationName, variables);
        return Mono.fromFuture(executor.execute(graphQLRequest));
    }

    @PostMapping(
            value = "${graphql.http.endpoint:/graphql}",
            consumes = {"application/graphql", "application/graphql;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Mono<Map<String, Object>> executeGraphQLPost(
            @RequestBody String queryBody,
            GraphQLRequest graphQLRequest,
            ServerWebExchange request
    ) {
        String query = graphQLRequest.getQuery() == null ? queryBody : graphQLRequest.getQuery();
        graphQLRequest = new GraphQLRequest(query, graphQLRequest.getOperationName(), graphQLRequest.getVariables());
        return Mono.fromFuture(executor.execute(graphQLRequest));
    }

    @RequestMapping(
            method = RequestMethod.POST,
            value = "${graphql.http.endpoint:/graphql}",
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, "application/x-www-form-urlencoded;charset=UTF-8"},
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public Mono<Map<String, Object>> executeFormPost(
            @RequestParam Map<String, String> queryParams,
            GraphQLRequest graphQLRequest,
            ServerWebExchange request
    ) {
        String queryParam = queryParams.get("query");
        String operationNameParam = queryParams.get("operationName");

        String query = StringUtils.isEmpty(queryParam) ? graphQLRequest.getQuery() : queryParam;
        String operationName = StringUtils.isEmpty(operationNameParam) ? graphQLRequest.getOperationName() :
                operationNameParam;

        graphQLRequest = new GraphQLRequest(query, operationName, graphQLRequest.getVariables());
        return Mono.fromFuture(executor.execute(graphQLRequest));
    }

    @GetMapping(
            value = "${graphql.http.endpoint:/graphql}",
            produces = MediaType.APPLICATION_JSON_VALUE,
            headers = "Connection!=Upgrade"
    )
    @ResponseBody
    public Mono<Map<String, Object>> executeGet(GraphQLRequest graphQLRequest, ServerWebExchange request) {
        return Mono.fromFuture(executor.execute(graphQLRequest));
    }
}
