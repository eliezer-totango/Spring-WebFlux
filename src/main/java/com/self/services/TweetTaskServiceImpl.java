package com.self.services;

import com.self.entities.Tweet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TweetTaskServiceImpl implements TweetTaskService {

    private static final Logger logger = LoggerFactory.getLogger(TweetTaskServiceImpl.class);
    private static final String UPDATE_SERVICE_DEFAULTS_URL = "http://localhost:8085/slow-service-Tweets/";
    private Map<String, AtomicInteger> serviceIdToRetryTimesMap = new ConcurrentHashMap<>();
    private DecimalFormat progressFormat = new DecimalFormat("#.#####");
    private ScheduledExecutorService executorService;
    @Value("${scheduler.thread.pool.size}")
    private Integer schedulerPoolSize;
    @Autowired
    private RestTemplate restTemplate;


    @PostConstruct()
    void init() {
        executorService = Executors.newScheduledThreadPool(schedulerPoolSize);
    }

    @Override
    public List<Tweet> getAllTweet(List<String> userIdsList){
            List<String> allServiceIdToUpdate = userIdsList;
            final List<CompletableFuture<List<Tweet>>> futures = executeTasks(allServiceIdToUpdate);
            List<Tweet> tweets = waitUntilFinishedAllTasksExecutions(futures);
            logger.info("done to get all tweets {}", Arrays.toString(tweets.toArray()));
            return tweets;
    }

    private List<CompletableFuture<List<Tweet>>> executeTasks(List<String> allUserIdToUpdate) {
        AtomicInteger updaterCounter = new AtomicInteger();
        List<CompletableFuture<List<Tweet>>> completableFutures = new ArrayList<>();
        for (String userId : allUserIdToUpdate) {
            CompletableFuture<List<Tweet>> futureResult = CompletableFuture
                    .supplyAsync(() -> updateServiceDefaults(userId, updaterCounter, allUserIdToUpdate.size())
                            , executorService)
                    .handle((res, ex) -> {
                        if (ex != null) {
                            printProgress(updaterCounter, allUserIdToUpdate.size());
                            return doOnError(userId, ex.getMessage());
                        }
                        return res;
                    });
            completableFutures.add(futureResult);
        }
        return completableFutures;
    }

    private void printProgress(AtomicInteger updaterCounter, int totalServicesToUpdate) {
        String progressPercentage = progressFormat.format(((double) updaterCounter.incrementAndGet() / totalServicesToUpdate) * 100);
        logger.info("progressPercentage: {}%", progressPercentage);
    }

    private List<Tweet> doOnError(final String serviceId, String errorMessage) {
        logger.error("failed to update service id {}. exception: {}", serviceId, errorMessage);
        return new ArrayList<>();
    }


    private List<Tweet> waitUntilFinishedAllTasksExecutions(final List<CompletableFuture<List<Tweet>>> futureResultList) {
        List<Tweet> allTweet = new ArrayList<>();
        for (CompletableFuture<List<Tweet>> future : futureResultList) {
            try {
                final List<Tweet> tweets = future.get();
                logger.info("done to update tweets {}", Arrays.toString(tweets.toArray()));
                allTweet.addAll(tweets);
            } catch (Exception e) {
                logger.error("failed duration updating ", e);
            }
        }
        return allTweet;
    }

    private List<Tweet> updateServiceDefaults(final String serviceId, AtomicInteger updaterCounter, final int totalServicesToUpdate) {
        List<Tweet> tweets;
        try {
            LocalDateTime start = LocalDateTime.now();
            tweets = restTemplate.getForObject(UPDATE_SERVICE_DEFAULTS_URL + serviceId, List.class);
            logger.info("serviceId '{}' update successfully. total time {}", serviceId, Duration.between(start, LocalDateTime.now()));
            printProgress(updaterCounter, totalServicesToUpdate);
        } catch (Exception ex) {
            return doOnError(serviceId, ex.getMessage());
        } finally {
            printProgress(updaterCounter, totalServicesToUpdate);
        }
        return tweets;
    }

    @PreDestroy
    private void destroy() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

}
