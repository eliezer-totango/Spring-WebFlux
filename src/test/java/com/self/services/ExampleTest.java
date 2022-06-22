package com.self.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.self.SelfApp;
import com.self.controllers.RoutingController;
import com.self.entities.Tweet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
public class ExampleTest {

    @Value("${server.port}")
    private String port;
    private static final String localhost = "http://localhost:";
    public static String selfFullUrl;

    @BeforeEach
    public void setUp() {
        selfFullUrl = localhost + port;
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TweetTaskService tweetTaskService;

    @Test
    public void objectMapperTest() {
        assertNotNull(objectMapper);
    }

}