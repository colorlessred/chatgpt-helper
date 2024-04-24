package com.synthetictruth.chatgptHelper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PromptTest {
    @Test
    public void PromptConstructor(){
        Prompt prompt = new Prompt("something\\nsecond line\\n\\n-- this is my, source", "prefix: %s");

        assertTrue(prompt.isHasSource());
        assertEquals("this_is_my_source", prompt.getSource());
        assertEquals("prefix: something\\nsecond line\\n\\n", prompt.getContent());
    }
}