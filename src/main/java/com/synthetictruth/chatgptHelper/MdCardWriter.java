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

    public static List<Card> parseSource(String source) {
        List<String> contents = Arrays.stream(source.split("---")).toList();
        return contents.stream().map(Card::new).toList();
    }

    @Getter
    static public class Card {
        public Card(String content) {
            String[] tokens = content.split("\\**(Front|Back)\\**:\\s*");
            if (tokens.length == 3) {
                this.front = tokens[1].trim();
                this.back = tokens[2].trim();
            } else {
                System.out.println("Cannot parse card: " + content);
                this.isParsedCorrectly = false;
            }
        }

        private String front;
        private String back;
        private boolean isParsedCorrectly = true;

        public String getContent() {
            return String.format("%s\n---\n%s", this.front, this.back);
        }
    }
}
