package com.synthetictruth.chatgptHelper;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * parse a file and produce multiple timestamped markdown files from it
 */
@AllArgsConstructor
public class MdCardWriter {
    private Path sourceFile;
    private Path targetDir;

    public void parse() throws IOException {
        String source = Files.readString(sourceFile);
        List<Card> cards = parseSource(source);
        // TODO write out the results
    }

    public static List<Card> parseSource(String source){
        List<String> contents = Arrays.stream(source.split("---")).toList();
        return contents.stream().map(Card::new).toList();
    }

    @Getter
    static public class Card {
        public Card(String content) {
            String[] tokens = content.split("\\**\\w:?\\**");
            if (tokens.length == 3) {
                this.front = tokens[1];
                this.back = tokens[2];
            } else {
                System.out.println("Cannot parse card: " + content);
            }
        }

        String front;
        String back;
    }
}
