package com.itheima.aiagent01.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkServiceTest {

    private final DocumentChunkService documentChunkService = new DocumentChunkService();

    @Test
    void shouldSplitTextIntoChunks() {
        String text = "abcdefghijklmnopqrstuvwxyz";

        List<String> chunks = documentChunkService.splitText(text, 10, 2);

        assertEquals(3, chunks.size());
        assertEquals("abcdefghij", chunks.get(0));
        assertEquals("ijklmnopqr", chunks.get(1));
        assertEquals("qrstuvwxyz", chunks.get(2));
    }

    @Test
    void shouldReturnEmptyListWhenTextIsBlank() {
        List<String> chunks = documentChunkService.splitText("", 10, 2);

        assertTrue(chunks.isEmpty());
    }

    @Test
    void shouldThrowExceptionWhenChunkSizeInvalid() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> documentChunkService.splitText("hello world", 0, 2)
        );

        assertEquals("分块大小必须大于 0", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOverlapInvalid() {
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> documentChunkService.splitText("hello world", 10, 10)
        );

        assertEquals("重叠大小必须大于等于 0 且小于分块大小", exception.getMessage());
    }
}