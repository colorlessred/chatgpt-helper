package com.synthetictruth.chatgptHelper;

import lombok.Getter;
import lombok.extern.java.Log;

import java.util.Arrays;
import java.util.List;

@Getter
@Log
public class Card {
    public Card(String content) {
        String[] tokens = content.split("\\**(Front|Back)\\**:\\s*");
        if (tokens.length == 3) {
            this.front = tokens[1].trim();
            this.back = tokens[2].trim();
        } else {
            log.severe("Cannot parse card: " + content);
            this.isParsedCorrectly = false;
        }
    }

    private String front;
    private String back;
    private boolean isParsedCorrectly = true;

    public String getContent() {
        return String.format("%s\n---\n%s", this.front, this.back);
    }

    public static List<Card> parseSource(String source) {
        List<String> contents = Arrays.stream(source.split("---")).toList();
        return contents.stream().map(Card::new).toList();
    }
}
