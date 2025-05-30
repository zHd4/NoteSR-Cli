package app.notesr.cli.command;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TablePrinterTest {
    @Test
    void testPrintTable() {
        List<String> headers = Arrays.asList("ID", "Name");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("1", "Alice"),
                Arrays.asList("2", "Bob")
        );

        TablePrinter printer = new TablePrinter();
        String output = printer.printTable(headers, rows);

        assertTrue(output.contains("ID"));
        assertTrue(output.contains("Alice"));
        assertTrue(output.contains("Bob"));
    }

    @Test
    void testPrintTableAlignment() {
        List<String> headers = Arrays.asList("Col1", "Column2");
        List<List<String>> rows = Arrays.asList(
                Arrays.asList("short", "very long text"),
                Arrays.asList("medium", "tiny")
        );

        TablePrinter printer = new TablePrinter();
        printer.setPadding(2);

        String output = printer.printTable(headers, rows);

        assertTrue(output.contains("very long text"));
        assertTrue(output.contains("short"));
    }

    @Test
    void testPrintTableWithoutRows() {
        List<String> headers = Arrays.asList("A", "B");
        List<List<String>> rows = List.of();

        TablePrinter printer = new TablePrinter();
        String output = printer.printTable(headers, rows);

        assertTrue(output.contains("A"));
        assertTrue(output.contains("+"));
    }
}
