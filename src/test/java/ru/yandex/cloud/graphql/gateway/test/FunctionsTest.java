package ru.yandex.cloud.graphql.gateway.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import ru.yandex.cloud.graphql.gateway.GraphQLController;
import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionRequest;
import ru.yandex.cloud.graphql.gateway.configuration.GraphQLConfiguration;
import ru.yandex.cloud.graphql.gateway.configuration.GraphqlApiYaml;
import ru.yandex.cloud.graphql.gateway.configuration.S3ClientConfiguration;
import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        GraphQLConfiguration.class,
        S3ClientConfiguration.class,
        GraphqlApiYaml.class,
        GraphQLController.class
})
@WebFluxTest(GraphQLController.class)
class FunctionsTest {

    public static final TypeReference<List<FunctionRequest>> FUNCTION_REQUESTS_TYPE_REF =
            new TypeReference<List<FunctionRequest>>() {
            };

    public static final ParameterizedTypeReference<Map<String, Object>> GRAPHQL_RESPONSE_TYPE_REF =
            new ParameterizedTypeReference<Map<String, Object>>() {
            };

    private static final String GET_POST_QUERY =
            "{" +
                    "    getPost(id: 234) {" +
                    "        id" +
                    "        title" +
                    "        date" +
                    "        published" +
                    "        type" +
                    "        views" +
                    "        rating" +
                    "        author {" +
                    "            id" +
                    "            name" +
                    "        }" +
                    "        relations {" +
                    "           ... on FilmRelation {" +
                    "               filmId" +
                    "           }" +
                    "           ... on ActorRelation {" +
                    "               actorId" +
                    "           }" +
                    "        }" +
                    "        tags" +
                    "    }" +
                    "}";

    private static final String CREATE_POST_QUERY =
            "mutation {" +
                    "    createPost(input: { title: \"Title\" }) {" +
                    "        id" +
                    "        title" +
                    "        date" +
                    "        published" +
                    "        type" +
                    "        views" +
                    "        rating" +
                    "        author {" +
                    "            id" +
                    "            name" +
                    "        }" +
                    "        relations {" +
                    "           ... on FilmRelation {" +
                    "               filmId" +
                    "           }" +
                    "           ... on ActorRelation {" +
                    "               actorId" +
                    "           }" +
                    "        }" +
                    "        tags" +
                    "    }" +
                    "}";

    private static final String FIND_POSTS_QUERY =
            "{" +
                    "  findPosts(filter: { published: true }) {" +
                    "    id" +
                    "    title" +
                    "    linkedPosts {" +
                    "      id" +
                    "      title" +
                    "    }" +
                    "  }" +
                    "}";

    private static final String INTROSPECTION_QUERY = "query IntrospectionQuery {\n" +
            "  __schema {\n" +
            "    queryType {\n" +
            "      name\n" +
            "    }\n" +
            "    mutationType {\n" +
            "      name\n" +
            "    }\n" +
            "    subscriptionType {\n" +
            "      name\n" +
            "    }\n" +
            "    types {\n" +
            "      ...FullType\n" +
            "    }\n" +
            "    directives {\n" +
            "      name\n" +
            "      description\n" +
            "      locations\n" +
            "      args {\n" +
            "        ...InputValue\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}\n" +
            "fragment FullType on __Type {\n" +
            "  kind\n" +
            "  name\n" +
            "  description\n" +
            "  fields(includeDeprecated: true) {\n" +
            "    name\n" +
            "    description\n" +
            "    args {\n" +
            "      ...InputValue\n" +
            "    }\n" +
            "    type {\n" +
            "      ...TypeRef\n" +
            "    }\n" +
            "    isDeprecated\n" +
            "    deprecationReason\n" +
            "  }\n" +
            "  inputFields {\n" +
            "    ...InputValue\n" +
            "  }\n" +
            "  interfaces {\n" +
            "    ...TypeRef\n" +
            "  }\n" +
            "  enumValues(includeDeprecated: true) {\n" +
            "    name\n" +
            "    description\n" +
            "    isDeprecated\n" +
            "    deprecationReason\n" +
            "  }\n" +
            "  possibleTypes {\n" +
            "    ...TypeRef\n" +
            "  }\n" +
            "}\n" +
            "fragment InputValue on __InputValue {\n" +
            "  name\n" +
            "  description\n" +
            "  type {\n" +
            "    ...TypeRef\n" +
            "  }\n" +
            "  defaultValue\n" +
            "}\n" +
            "fragment TypeRef on __Type {\n" +
            "  kind\n" +
            "  name\n" +
            "  ofType {\n" +
            "    kind\n" +
            "    name\n" +
            "    ofType {\n" +
            "      kind\n" +
            "      name\n" +
            "      ofType {\n" +
            "        kind\n" +
            "        name\n" +
            "        ofType {\n" +
            "          kind\n" +
            "          name\n" +
            "          ofType {\n" +
            "            kind\n" +
            "            name\n" +
            "            ofType {\n" +
            "              kind\n" +
            "              name\n" +
            "              ofType {\n" +
            "                kind\n" +
            "                name\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static MockWebServer mockBooksApi;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void beforeEach() throws IOException {
        mockBooksApi = new MockWebServer();
        mockBooksApi.start(8081);
    }

    @AfterEach
    void afterEach() throws IOException {
        mockBooksApi.shutdown();
    }

    @Test
    void testQueryOk() throws InterruptedException, JsonProcessingException {
        Map<String, Object> expectedPost = new HashMap<>();
        expectedPost.put("id", "1");
        expectedPost.put("title", "Title");
        expectedPost.put("date", "01.10.2020");
        expectedPost.put("published", true);
        expectedPost.put("type", "news");
        expectedPost.put("views", 1000);
        expectedPost.put("rating", 9.9);
        HashMap<Object, Object> author = new HashMap<>();
        author.put("id", "1");
        author.put("name", "Ivan Pupkin");
        expectedPost.put("author", author);
        HashMap<Object, Object> actorRelation = new HashMap<>();
        actorRelation.put("actorId", "1");
        actorRelation.put("type", "ActorRelation");
        HashMap<Object, Object> filmRelation = new HashMap<>();
        filmRelation.put("filmId", "1");
        filmRelation.put("type", "FilmRelation");
        expectedPost.put("relations", Arrays.asList(actorRelation, filmRelation));
        expectedPost.put("tags", Arrays.asList("politic", "music"));

        mockBooksApi.enqueue(
                new MockResponse()
                        .setBody(objectMapper.writeValueAsString(expectedPost))
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        GraphQLRequest request = new GraphQLRequest(GET_POST_QUERY, null, Collections.emptyMap(),
                Collections.emptyMap());

        webTestClient.post()
                .uri("/graphql")
                .bodyValue(request)
                //.header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GRAPHQL_RESPONSE_TYPE_REF)
                .value(response -> {
                    Map<String, Object> data = getValue(response, "data");
                    Map<String, Object> actualPost = getValue(data, "getPost");

                    filmRelation.remove("type");
                    actorRelation.remove("type");
                    assertEquals(expectedPost, actualPost);

                    List<Map<String, Object>> errors = getValue(response, "errors");
                    assertNull(errors);
                });

        RecordedRequest recordedRequest = mockBooksApi.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/d4ee6ud2345m1kak1ltq?integration=raw", recordedRequest.getPath());

        FunctionRequest functionRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(),
                FunctionRequest.class);
        assertEquals("Query", functionRequest.getParentType());
        assertEquals("getPost", functionRequest.getField().getName());
        assertEquals("id", functionRequest.getField().getArguments().get(0).getName());
        assertEquals(234, functionRequest.getField().getArguments().get(0).getValue());
    }

    @Test
    void testQueryError() throws InterruptedException, JsonProcessingException {
        Map<String, Object> functionError = new HashMap<>();
        functionError.put("errorMessage", "Exception message");
        functionError.put("errorType", "Error");
        Map<String, Object> stackTrace = new HashMap<>();
        stackTrace.put("function", "Runtime.exports.handler");
        stackTrace.put("file", "/function/code/index.js");
        stackTrace.put("line", 6);
        stackTrace.put("column", 11);
        functionError.put("stackTrace", Collections.singletonList(stackTrace));

        mockBooksApi.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_GATEWAY.value())
                .setBody(objectMapper.writeValueAsString(functionError))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        GraphQLRequest request = new GraphQLRequest(GET_POST_QUERY, null, Collections.emptyMap(),
                Collections.emptyMap());

        webTestClient.post()
                .uri("/graphql")
                .bodyValue(request)
                //.header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GRAPHQL_RESPONSE_TYPE_REF)
                .value(response -> {
                    List<Map<String, Object>> errors = getValue(response, "errors");
                    Map<String, Object> graphQLError = errors.get(0);
                    Map<String, Object> extensions = getValue(graphQLError, "extensions");

                    assertEquals(extensions.get("classification"), "FunctionError");
                    assertEquals(graphQLError.get("message"), functionError.get("errorMessage"));
                    assertEquals(extensions.get("errorType"), functionError.get("errorType"));
                    assertEquals(graphQLError.get("path"), Collections.singletonList("getPost"));
                    assertEquals(graphQLError.get("locations"), Collections.singletonList(ImmutableMap.<String,
                                    Object>builder()
                                    .put("sourceName", "/function/code/index.js")
                                    .put("line", 6)
                                    .put("column", 11)
                                    .build()
                            )
                    );
                });

        RecordedRequest recordedRequest = mockBooksApi.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/d4ee6ud2345m1kak1ltq?integration=raw", recordedRequest.getPath());
    }

    @Test
    void testMutationOk() throws InterruptedException, JsonProcessingException {
        Map<String, Object> expectedPost = new HashMap<>();
        expectedPost.put("id", "1");
        expectedPost.put("title", "Title");
        expectedPost.put("date", "01.10.2020");
        expectedPost.put("published", true);
        expectedPost.put("type", "news");
        expectedPost.put("views", 1000);
        expectedPost.put("rating", 9.9);
        HashMap<Object, Object> author = new HashMap<>();
        author.put("id", "1");
        author.put("name", "Ivan Pupkin");
        expectedPost.put("author", author);
        HashMap<Object, Object> actorRelation = new HashMap<>();
        actorRelation.put("actorId", "1");
        actorRelation.put("type", "ActorRelation");
        HashMap<Object, Object> filmRelation = new HashMap<>();
        filmRelation.put("filmId", "1");
        filmRelation.put("type", "FilmRelation");
        expectedPost.put("relations", Arrays.asList(actorRelation, filmRelation));
        expectedPost.put("tags", Arrays.asList("politic", "music"));

        mockBooksApi.enqueue(
                new MockResponse()
                        .setBody(objectMapper.writeValueAsString(expectedPost))
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        GraphQLRequest request = new GraphQLRequest(CREATE_POST_QUERY, null, Collections.emptyMap(),
                Collections.emptyMap());

        webTestClient.post()
                .uri("/graphql")
                .bodyValue(request)
                //.header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GRAPHQL_RESPONSE_TYPE_REF)
                .value(response -> {
                    Map<String, Object> data = getValue(response, "data");
                    Map<String, Object> actualPost = getValue(data, "createPost");

                    filmRelation.remove("type");
                    actorRelation.remove("type");
                    assertEquals(expectedPost, actualPost);

                    List<Map<String, Object>> errors = getValue(response, "errors");
                    assertNull(errors);
                });

        RecordedRequest recordedRequest = mockBooksApi.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/d4e8be3qvb0ndkma8jnf?integration=raw", recordedRequest.getPath());

        FunctionRequest functionRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(),
                FunctionRequest.class);
        assertEquals("Mutation", functionRequest.getParentType());
        assertEquals("createPost", functionRequest.getField().getName());
        assertEquals("input", functionRequest.getField().getArguments().get(0).getName());
        assertEquals(ImmutableMap.<String, Object>builder().put("title", "Title").build(),
                functionRequest.getField().getArguments().get(0).getValue());
    }

    @Test
    void testBatchQuery() throws InterruptedException, JsonProcessingException {
        Map<String, Object> post1 = new HashMap<>(ImmutableMap.<String, Object>builder()
                .put("id", "1")
                .put("title", "Title1")
                .build()
        );
        Map<String, Object> post2 = new HashMap<>(ImmutableMap.<String, Object>builder()
                .put("id", "2")
                .put("title", "Title2")
                .build());

        mockBooksApi.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(Arrays.asList(post1, post2)))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        Map<String, Object> post11 = ImmutableMap.<String, Object>builder()
                .put("id", "11")
                .put("title", "LinkedPostTitle11")
                .build();
        Map<String, Object> post12 = ImmutableMap.<String, Object>builder()
                .put("id", "12")
                .put("title", "LinkedPostTitle12")
                .build();
        Map<String, Object> linkedPosts = ImmutableMap.<String, Object>builder()
                .put("data", Arrays.asList(post11, post12))
                .build();
        Map<String, Object> error = ImmutableMap.<String, Object>builder()
                .put("errorMessage", "Unknown error is occurred")
                .put("errorType", "UnknownError")
                .build();
        mockBooksApi.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(Arrays.asList(linkedPosts, error)))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        GraphQLRequest request = new GraphQLRequest(FIND_POSTS_QUERY, null, Collections.emptyMap(),
                Collections.emptyMap());

        webTestClient.post()
                .uri("/graphql")
                .bodyValue(request)
                //.header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GRAPHQL_RESPONSE_TYPE_REF)
                .value(response -> {
                    Map<String, Object> data = getValue(response, "data");
                    List<Map<String, Object>> foundPosts = getValue(data, "findPosts");

                    post1.put("linkedPosts", Arrays.asList(post11, post12));
                    post2.put("linkedPosts", null);
                    assertEquals(Arrays.asList(post1, post2), foundPosts);

                    List<Map<String, Object>> errors = getValue(response, "errors");
                    Map<String, Object> graphQLError = errors.get(0);
                    Map<String, Object> extensions = getValue(graphQLError, "extensions");

                    assertEquals(graphQLError.get("message"), error.get("errorMessage"));
                    assertEquals(graphQLError.get("path"), Arrays.asList("findPosts", 1, "linkedPosts"));
                    assertEquals(extensions.get("classification"), "FunctionError");
                    assertEquals(extensions.get("errorType"), error.get("errorType"));
                });

        RecordedRequest findPostsRequest = mockBooksApi.takeRequest();
        assertEquals("POST", findPostsRequest.getMethod());
        assertEquals("/d4e4th99piq42s782h6i?integration=raw", findPostsRequest.getPath());

        FunctionRequest findPostsFunctionRequest = objectMapper.readValue(findPostsRequest.getBody().readUtf8(),
                FunctionRequest.class);
        assertEquals("Query", findPostsFunctionRequest.getParentType());
        assertEquals("findPosts", findPostsFunctionRequest.getField().getName());
        assertEquals("filter", findPostsFunctionRequest.getField().getArguments().get(0).getName());
        assertEquals(ImmutableMap.<String, Object>builder().put("published", true).build(),
                findPostsFunctionRequest.getField().getArguments().get(0).getValue());

        RecordedRequest getLinkedPostsRequest = mockBooksApi.takeRequest();
        assertEquals("POST", getLinkedPostsRequest.getMethod());
        assertEquals("/d4e5fsgu6foi3jql8hak?integration=raw", getLinkedPostsRequest.getPath());

        List<FunctionRequest> functionRequests = objectMapper.readValue(getLinkedPostsRequest.getBody().readUtf8(),
                FUNCTION_REQUESTS_TYPE_REF);

        FunctionRequest functionRequest1 = functionRequests.get(0);
        assertEquals("Post", functionRequest1.getParentType());
        assertEquals("linkedPosts", functionRequest1.getField().getName());
        assertEquals("1", functionRequest1.getSource().get("id"));
        assertEquals("Title1", functionRequest1.getSource().get("title"));
        FunctionRequest functionRequest2 = functionRequests.get(1);
        assertEquals("Post", functionRequest2.getParentType());
        assertEquals("linkedPosts", functionRequest2.getField().getName());
        assertEquals("2", functionRequest2.getSource().get("id"));
        assertEquals("Title2", functionRequest2.getSource().get("title"));
    }

    @Test
    void testIntrospectionQuery() {
        GraphQLRequest request = new GraphQLRequest(INTROSPECTION_QUERY, "IntrospectionQuery", Collections.emptyMap()
                , Collections.emptyMap());

        webTestClient.post()
                .uri("/graphql")
                .bodyValue(request)
                //.header("Content-Type", "application/json")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GRAPHQL_RESPONSE_TYPE_REF)
                .value(response -> {
                    Map<String, Object> data = getValue(response, "data");
                    assertNotNull(getValue(data, "__schema"));
                });
    }

    <T> T getValue(Map<String, Object> map, String key) {
        return (T) map.get(key);
    }
}
