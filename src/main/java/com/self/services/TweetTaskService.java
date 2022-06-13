package com.self.services;

import com.self.entities.Tweet;

import java.util.List;

public interface TweetTaskService {
    List<Tweet> getAllTweet(List<String> userIdsList);
}
