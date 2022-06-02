package com.self.services;

import com.self.SelfApp;
import com.self.controllers.RoutingController;
import com.self.entities.Tweet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT, classes = SelfApp.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ReteLimitedTest {

    @Value("${server.port}")
    private String port;
    private static final String localhost = "http://localhost:";
    public static String perimeterXFullUrl;

    @BeforeEach
    public void setUp() {
        perimeterXFullUrl = localhost + port;
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private RoutingController routingController;

    @Test
    public void reteLimitedTest() throws InterruptedException {
        List<Tweet> tweetsNonBlocking = routingController.getTweetsNonBlocking();
    }

}