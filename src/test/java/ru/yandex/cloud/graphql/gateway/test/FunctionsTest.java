package ru.yandex.cloud.graphql.gateway.test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import ru.yandex.cloud.graphql.gateway.GraphQLExecutor;
import ru.yandex.cloud.graphql.gateway.client.functions.model.FunctionRequest;
import ru.yandex.cloud.graphql.gateway.model.GraphQLRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("test")
@SpringBootTest(properties = {
        "functions.api.url=http://localhost:8081",
        "graphql.api.config.location=post-api.yml"
})
class FunctionsTest {

    public static final TypeReference<List<FunctionRequest>> FUNCTION_REQUESTS_TYPE_REF = new TypeReference<>() {
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

    private static MockWebServer mockBooksApi;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GraphQLExecutor graphQLExecutor;

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
    void testQueryOk() throws ExecutionException, InterruptedException, JsonProcessingException {
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

        GraphQLRequest request = GraphQLRequest.builder()
                .query(GET_POST_QUERY)
                .variables(new HashMap<>())
                .build();

        Map<String, Object> response = graphQLExecutor.execute(request).get();

        RecordedRequest recordedRequest = mockBooksApi.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/d4ee6ud2345m1kak1ltq?integration=raw", recordedRequest.getPath());

        FunctionRequest functionRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(),
                FunctionRequest.class);
        assertEquals("Query", functionRequest.getParentType());
        assertEquals("getPost", functionRequest.getField().getName());
        assertEquals("id", functionRequest.getField().getArguments().get(0).getName());
        assertEquals(234, functionRequest.getField().getArguments().get(0).getValue());

        Map<String, Object> data = getValue(response, "data");
        Map<String, Object> actualPost = getValue(data, "getPost");

        filmRelation.remove("type");
        actorRelation.remove("type");
        assertEquals(expectedPost, actualPost);

        List<Map<String, Object>> errors = getValue(response, "errors");
        assertNull(errors);
    }

    @Test
    void testQueryError() throws ExecutionException, InterruptedException, JsonProcessingException {
        Map<String, Object> functionError = new HashMap<>();
        functionError.put("errorMessage", "Exception message");
        functionError.put("errorType", "Error");
        functionError.put("stackTrace", Collections.singletonList(Map.of(
                "function", "Runtime.exports.handler",
                "file", "/function/code/index.js",
                "line", 6,
                "column", 11))
        );

        mockBooksApi.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.BAD_GATEWAY.value())
                .setBody(objectMapper.writeValueAsString(functionError))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        GraphQLRequest request = GraphQLRequest.builder()
                .query(GET_POST_QUERY)
                .variables(new HashMap<>())
                .build();

        Map<String, Object> response = graphQLExecutor.execute(request).get();

        RecordedRequest recordedRequest = mockBooksApi.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/d4ee6ud2345m1kak1ltq?integration=raw", recordedRequest.getPath());

        List<Map<String, Object>> errors = getValue(response, "errors");
        Map<String, Object> graphQLError = errors.get(0);
        Map<String, Object> extensions = getValue(graphQLError, "extensions");

        assertEquals(extensions.get("classification"), "FunctionError");
        assertEquals(graphQLError.get("message"), functionError.get("errorMessage"));
        assertEquals(extensions.get("errorType"), functionError.get("errorType"));
        assertEquals(graphQLError.get("path"), Collections.singletonList("getPost"));
        assertEquals(graphQLError.get("locations"), Collections.singletonList(Map.of(
                "sourceName", "/function/code/index.js",
                "line", 6,
                "column", 11
        )));
    }

    @Test
    void testMutationOk() throws ExecutionException, InterruptedException, JsonProcessingException {
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

        GraphQLRequest request = GraphQLRequest.builder()
                .query(CREATE_POST_QUERY)
                .variables(new HashMap<>())
                .build();

        Map<String, Object> response = graphQLExecutor.execute(request).get();

        RecordedRequest recordedRequest = mockBooksApi.takeRequest();
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals("/d4e8be3qvb0ndkma8jnf?integration=raw", recordedRequest.getPath());

        FunctionRequest functionRequest = objectMapper.readValue(recordedRequest.getBody().readUtf8(),
                FunctionRequest.class);
        assertEquals("Mutation", functionRequest.getParentType());
        assertEquals("createPost", functionRequest.getField().getName());
        assertEquals("input", functionRequest.getField().getArguments().get(0).getName());
        assertEquals(Map.of("title", "Title"), functionRequest.getField().getArguments().get(0).getValue());

        Map<String, Object> data = getValue(response, "data");
        Map<String, Object> actualPost = getValue(data, "createPost");

        filmRelation.remove("type");
        actorRelation.remove("type");
        assertEquals(expectedPost, actualPost);

        List<Map<String, Object>> errors = getValue(response, "errors");
        assertNull(errors);
    }

    @Test
    void testBatchQuery() throws ExecutionException, InterruptedException, JsonProcessingException {
        Map<String, Object> post1 = new HashMap<>(Map.of("id", "1", "title", "Title1"));
        Map<String, Object> post2 = new HashMap<>(Map.of("id", "2", "title", "Title2"));

        mockBooksApi.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(Arrays.asList(post1, post2)))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        Map<String, Object> post11 = Map.of("id", "11", "title", "LinkedPostTitle11");
        Map<String, Object> post12 = Map.of("id", "12", "title", "LinkedPostTitle12");
        Map<String, Object> linkedPosts = Map.of("data", Arrays.asList(post11, post12));
        Map<String, Object> error = Map.of(
                "errorMessage", "Unknown error is occurred",
                "errorType", "UnknownError"
        );
        mockBooksApi.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(Arrays.asList(linkedPosts, error)))
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        );

        GraphQLRequest request = GraphQLRequest.builder()
                .query(FIND_POSTS_QUERY)
                .variables(new HashMap<>())
                .build();

        Map<String, Object> response = graphQLExecutor.execute(request).get();

        RecordedRequest findPostsRequest = mockBooksApi.takeRequest();
        assertEquals("POST", findPostsRequest.getMethod());
        assertEquals("/d4e4th99piq42s782h6i?integration=raw", findPostsRequest.getPath());

        FunctionRequest findPostsFunctionRequest = objectMapper.readValue(findPostsRequest.getBody().readUtf8(),
                FunctionRequest.class);
        assertEquals("Query", findPostsFunctionRequest.getParentType());
        assertEquals("findPosts", findPostsFunctionRequest.getField().getName());
        assertEquals("filter", findPostsFunctionRequest.getField().getArguments().get(0).getName());
        assertEquals(Map.of("published", true), findPostsFunctionRequest.getField().getArguments().get(0).getValue());

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
    }

    <T> T getValue(Map<String, Object> map, String key) {
        return (T) map.get(key);
    }
}
