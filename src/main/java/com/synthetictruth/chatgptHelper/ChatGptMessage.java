package com.synthetictruth.chatgptHelper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChatGptMessage {
    public enum roles {system, user, assistant}

    String role;
    String content;
}
