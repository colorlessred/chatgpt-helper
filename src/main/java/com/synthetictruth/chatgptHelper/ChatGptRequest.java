package com.synthetictruth.chatgptHelper;

import lombok.Builder;
import lombok.Singular;

import java.util.List;

@Builder
public class ChatGptRequest {

    public static class UserMessage extends ChatGptMessage {
        public UserMessage(String content){
            super(String.valueOf(roles.user), content);
        }
    }

    String model;
    String prompt;
    double temperature;
    int max_tokens;

    @Singular
    List<ChatGptMessage> messages;
}
