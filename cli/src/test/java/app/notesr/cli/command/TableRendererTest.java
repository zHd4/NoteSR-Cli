/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.command;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TableRendererTest {
    @Test
    void testRender() {
        List<String> headers = Arrays.asList("ID", "Name");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("1", "Alice"),
                Arrays.asList("2", "Bob")
        );

        TableRenderer printer = new TableRenderer();
        String output = printer.render(headers, rows);

        assertTrue(output.contains("ID"));
        assertTrue(output.contains("Alice"));
        assertTrue(output.contains("Bob"));
    }

    @Test
    void testRenderAlignment() {
        List<String> headers = Arrays.asList("Col1", "Column2");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("short", "very long text"),
                Arrays.asList("medium", "tiny")
        );

        TableRenderer printer = new TableRenderer();
        printer.setPadding(2);

        String output = printer.render(headers, rows);

        assertTrue(output.contains("very long text"));
        assertTrue(output.contains("short"));
    }

    @Test
    void testRenderWithoutRows() {
        List<String> headers = Arrays.asList("A", "B");
        List<List<String>> rows = List.of();

        TableRenderer printer = new TableRenderer();
        String output = printer.render(headers, rows);

        assertTrue(output.contains("A"));
        assertTrue(output.contains("+"));
    }
}
