package com.synthetictruth.chatgptHelper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChatGptHelperTest {

    @Test
    public void createTagTest() {
        assertEquals("#src_abc_def", ChatGptHelper.computeTag("https://www.abc(def) www Abc"));
    }

}