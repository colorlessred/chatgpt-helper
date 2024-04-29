package com.synthetictruth.chatgptHelper;

import lombok.Getter;

@Getter
public class Prompt {
    private String content;
    private String source;
    private boolean hasSource = false;

    public Prompt(String source, String prefixFormat) {
        String[] tokens = source.split("--\s");

        this.content = String.format(prefixFormat, tokens[0]);

        if (tokens.length > 1) {
            this.hasSource = true;
            this.source = tokens[1];
        }
    }
}
