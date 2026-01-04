/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.command;

import lombok.Setter;

import java.util.List;

@Setter
final class TableRenderer {
    private int padding = 1;

    public String render(List<String> headers, List<List<String>> rows) {
        int[] columnWidths = computeColumnWidths(headers, rows);

        StringBuilder builder = new StringBuilder();
        String line = buildHorizontalLine(columnWidths);

        builder.append(line).append("\n");
        builder.append(buildRow(headers, columnWidths)).append("\n");
        builder.append(line).append("\n");

        for (List<String> row : rows) {
            builder.append(buildRow(row, columnWidths)).append("\n");
        }

        builder.append(line);
        return builder.toString();
    }

    private int[] computeColumnWidths(List<String> headers, List<List<String>> rows) {
        int columnCount = headers.size();
        int[] widths = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            widths[i] = headers.get(i).length();
        }

        for (List<String> row : rows) {
            for (int i = 0; i < columnCount; i++) {
                widths[i] = Math.max(widths[i], row.get(i).length());
            }
        }

        for (int i = 0; i < columnCount; i++) {
            widths[i] += 2 * padding;
        }

        return widths;
    }

    private String buildHorizontalLine(int[] columnWidths) {
        StringBuilder builder = new StringBuilder();
        builder.append("+");

        for (int width : columnWidths) {
            builder.append("-"
                    .repeat(width + 2))
                    .append("+");
        }

        return builder.toString();
    }

    private String buildRow(List<String> columns, int[] columnWidths) {
        StringBuilder builder = new StringBuilder();
        builder.append("|");

        for (int i = 0; i < columns.size(); i++) {
            String cell = " ".repeat(padding) + columns.get(i) + " ".repeat(padding);
            builder.append(" ").append(padRight(cell, columnWidths[i])).append(" |");
        }

        return builder.toString();
    }

    private String padRight(String text, int length) {
        return String.format("%-" + length + "s", text);
    }
}
