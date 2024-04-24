package com.synthetictruth.chatgptHelper;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MdCardWriterTest {
    @Test
    public void parseOneCard(){
        String content = """
                Front: this is the front
                Back: this is the back
                """;

        List<Card> cards = Card.parseSource(content);
        assertEquals(1, cards.size());
        Card card = cards.get(0);
        assertEquals("this is the front", card.getFront());
        assertEquals("this is the back", card.getBack());
    }

    @Test
    public void parseOneCardWithAsterisks(){
        String content = """
                **Front**: this is the front
                **Back**: this is the back
                """;

        List<Card> cards = Card.parseSource(content);
        assertEquals(1, cards.size());
        Card card = cards.get(0);
        assertEquals("this is the front", card.getFront());
        assertEquals("this is the back", card.getBack());
    }


}