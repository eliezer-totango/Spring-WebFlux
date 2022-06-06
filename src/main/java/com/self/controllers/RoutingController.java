package com.self.controllers;

import com.self.entities.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@RestController
@RequestMapping()
public class RoutingController {

    private static final Logger logger = LoggerFactory.getLogger(RoutingController.class);
    private static final int maxRetry = 3;
    @Autowired
    private WebClient webClient;

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping(value = "/getTweetsNonBlocking")
    public List<Tweet> getTweetsNonBlocking() {
        logger.info("Starting NON-BLOCKING Controller!");
        List<Integer> userIdList = IntStream.range(1, 5).boxed().toList();
        List<Tweet> tweetList = Flux.fromIterable(userIdList)
                .flatMap(userId -> webClient
                                .get()
                                .uri("http://localhost:8085/slow-service-Tweets/" + userId)
                                .retrieve()
                                .bodyToFlux(Tweet.class)
//                                .retry(maxRetry)
//                                .doOnError(e -> logger.error("failed to get data for user-id {}", userIdList.get(i)))
                                .onErrorResume(e -> {
                                    logger.error("failed to get data for user-id {} ERROR {}", userId, e);
                                    return Flux.empty();
                                })
                                .doOnComplete(() -> logger.info("Complete to get Tweet data {} for user-id {}", userId))
                                .doOnNext(tweet -> logger.info("doOnNext starting to get Tweet data {} for user-id {}", tweet, userId))

                        , 100, 1)
                .collectList()
                .block();


//        Disposable subscribe = TweetFlux.subscribe(Tweet -> logger.info(Tweet.toString()));
//        disposableList.add(subscribe);

        logger.info("Exiting NON-BLOCKING Controller!");
        return tweetList;
    }

//        int count = 0;
//        for (Disposable subscribe : disposableList) {
//            while (!subscribe.isDisposed() && count < 100) {
//                Thread.sleep(400);
//                count++;
//                System.out.println("Waiting......");
//            }
//        }

    @GetMapping("/slow-service-Tweets/{userId}")
    private List<Tweet> getAllTweets(@PathVariable int userId) throws Exception {
        logger.info("getAllTweets of user {}", userId);
//        if (userId == 1) {
//            throw new RuntimeException("failed for user id: " + userId);
//        }
        Thread.sleep(5000L); // delay
        return Arrays.asList(
                new Tweet("RestTemplate rules", userId + "@gmail.com"),
                new Tweet("WebClient is better", userId + "@gmail.com"),
                new Tweet("OK, both are useful", userId + "@gmail.com"));
    }

}
