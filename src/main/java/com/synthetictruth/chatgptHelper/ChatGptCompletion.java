package com.synthetictruth.chatgptHelper;

import com.google.gson.Gson;
import lombok.Getter;

import java.util.List;

public class ChatGptCompletion {
    public class Choice {
        private int index;
        @Getter
        private ChatGptMessage message;
        private Object logprobs;
        private String finish_reason;
    }

    String id;
    @Getter
    List<Choice> choices;
    @Getter
    private ChatGptError error;

    public boolean hasError() {
        return this.error != null;
    }

    public static ChatGptCompletion fromString(String completion) {
        Gson gson = new Gson();
        return gson.fromJson(completion, ChatGptCompletion.class);
    }
}
