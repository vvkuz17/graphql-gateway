package ru.yandex.cloud.graphql.gateway;

import java.util.List;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;

import ru.yandex.cloud.graphql.gateway.model.GraphQLApiError;
import ru.yandex.cloud.graphql.gateway.transformer.error.GraphQLApiErrorTransformer;

@Slf4j
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = handlerParameters.getException();
        List<Object> path = handlerParameters.getPath().toList();

        GraphQLApiError error = GraphQLApiErrorTransformer.transform(exception, path);
        log.error(logMessage(exception, error), exception);

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

    private static String logMessage(Throwable exception, GraphQLApiError apiError) {
        return apiError.getErrorType() + " " +
                Strings.join(apiError.getPath(), '_') + "\n" +
                getMessageWithSuppressed(exception);
    }

    private static String getMessageWithSuppressed(Throwable throwable) {
        StringBuilder message = new StringBuilder()
                .append(throwable.getClass().getName())
                .append(" : ")
                .append(throwable.getMessage());

        for (Throwable suppressed : throwable.getSuppressed()) {
            message
                    .append("\n")
                    .append(suppressed.getClass().getName())
                    .append(" : ")
                    .append(suppressed.getMessage());
        }

        return message.toString();
    }
}
