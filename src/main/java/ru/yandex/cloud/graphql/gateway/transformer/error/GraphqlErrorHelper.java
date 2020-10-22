package ru.yandex.cloud.graphql.gateway.transformer.error;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import static java.util.stream.Collectors.toList;

public class GraphqlErrorHelper {

    public static Map<String, Object> toSpecification(GraphQLError error) {
        Map<String, Object> errorMap = new LinkedHashMap<>();
        errorMap.put("message", error.getMessage());
        if (error.getLocations() != null) {
            errorMap.put("locations", locations(error.getLocations()));
        }
        if (error.getPath() != null) {
            errorMap.put("path", error.getPath());
        }

        Map<String, Object> extensions = error.getExtensions();
        ErrorClassification errorClassification = error.getErrorType();
        //
        // we move the ErrorClassification into extensions which allows
        // downstream people to see them but still be spec compliant
        if (errorClassification != null) {
            if (extensions != null) {
                extensions = new LinkedHashMap<>(extensions);
            } else {
                extensions = new LinkedHashMap<>();
            }
            // put in the classification unless its already there
            if (!extensions.containsKey("classification")) {
                extensions.put("classification", errorClassification.toSpecification(error));
            }
        }

        if (extensions != null) {
            errorMap.put("extensions", extensions);
        }
        return errorMap;
    }

    public static Object locations(List<SourceLocation> locations) {
        return locations.stream().map(GraphqlErrorHelper::location).collect(toList());
    }

    public static Object location(SourceLocation location) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("sourceName", location.getSourceName());
        map.put("line", location.getLine());
        map.put("column", location.getColumn());
        return map;
    }
}
