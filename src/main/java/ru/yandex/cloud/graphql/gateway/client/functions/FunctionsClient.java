package ru.yandex.cloud.graphql.gateway.client.functions;

import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionResponse;
import ru.yandex.cloud.graphql.gateway.client.functions.exception.FunctionException;
import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionRequest;

public class FunctionsClient<V> {

    private final ParameterizedTypeReference<V> VALUE_TYPE_REFERENCE = new ParameterizedTypeReference<V>() {
    };
    private final ParameterizedTypeReference<List<FunctionResponse<V>>> BATCH_TYPE_REFERENCE =
            new ParameterizedTypeReference<List<FunctionResponse<V>>>() {
            };

    private final ParameterizedTypeReference<FunctionResponse<V>> ERROR_RESPONSE_TYPE_REFERENCE =
            new ParameterizedTypeReference<FunctionResponse<V>>() {
            };

    private final WebClient webclient;

    public FunctionsClient(String functionsApiUrl, String functionId) {
        this.webclient = WebClient.builder()
                //.clientConnector(new ReactorClientHttpConnector(HttpClient.create().wiretap(true)))
                .exchangeStrategies(ExchangeStrategies.builder().codecs(configurer -> {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    configurer.customCodecs().register(new Jackson2JsonDecoder(mapper,
                            MimeTypeUtils.parseMimeType(MediaType.TEXT_PLAIN_VALUE)));
                }).build())
                .baseUrl(functionsApiUrl + "/" + functionId + "?integration=raw")
                .build();
    }

    public Mono<V> invoke(FunctionRequest request) {
        return webclient
                .method(HttpMethod.POST)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::isError, this::handleError)
                .bodyToMono(VALUE_TYPE_REFERENCE);
    }

    public Mono<List<FunctionResponse<V>>> batchInvoke(List<FunctionRequest> requests) {
        return webclient
                .method(HttpMethod.POST)
                .bodyValue(requests)
                .retrieve()
                .onStatus(HttpStatus::isError, this::handleError)
                .bodyToMono(BATCH_TYPE_REFERENCE);
    }

    private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
        return clientResponse
                .bodyToMono(ERROR_RESPONSE_TYPE_REFERENCE)
                .flatMap(functionResponse -> Mono.error(FunctionException.fromResponse(functionResponse)));
    }
}
