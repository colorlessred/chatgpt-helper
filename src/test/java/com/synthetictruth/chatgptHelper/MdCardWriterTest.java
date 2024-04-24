package com.synthetictruth.chatgptHelper;

import com.synthetictruth.chatgptHelper.MdCardWriter.Card;
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

        List<Card> cards = MdCardWriter.parseSource(content);
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

        List<Card> cards = MdCardWriter.parseSource(content);
        assertEquals(1, cards.size());
        Card card = cards.get(0);
        assertEquals("this is the front", card.getFront());
        assertEquals("this is the back", card.getBack());
    }


}