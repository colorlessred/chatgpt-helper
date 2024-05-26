package com.synthetictruth.chatgptHelper;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ChatGptError {
    String message;
    String type;
    String param;
    String code;

    /**
     *   "error": {
     *     "message": "max_tokens is too large: 5000. This model supports at most 4096 completion tokens, whereas you provided 5000.",
     *     "type": null,
     *     "param": "max_tokens",
     *     "code": null
     *   }
     */
}
