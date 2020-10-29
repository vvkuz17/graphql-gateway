package ru.yandex.cloud.graphql.gateway.test;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import ru.yandex.cloud.graphql.gateway.GraphQLExecutor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
class ApplicationTest {

    @Autowired
    private GraphQLExecutor graphQLExecutor;

    @Test
    public void testApplicationContext() {
        assertNotNull(graphQLExecutor);
    }
}
