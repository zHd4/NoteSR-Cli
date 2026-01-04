/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.data;

import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DbConnectionTest {

    @Test
    void testDbStructureAgainstExpectedSchema(@TempDir Path tempDir) {
        DbConnection db = new DbConnection(tempDir.resolve("test.db").toString());
        Jdbi jdbi = db.getConnection();

        Map<String, List<String>> expectedSchema = Map.of(
                "notes", List.of("id", "name", "text", "updated_at"),
                "files_info", List.of("id", "note_id", "name", "type", "thumbnail", "size", "created_at", "updated_at"),
                "data_blocks", List.of("id", "file_id", "block_order", "data")
        );

        Set<String> actualTables = new HashSet<>(jdbi.withHandle(handle ->
                handle.createQuery("SELECT name FROM sqlite_master WHERE type='table'")
                        .mapTo(String.class)
                        .list()
        ));

        for (Map.Entry<String, List<String>> entry : expectedSchema.entrySet()) {
            String tableName = entry.getKey();
            List<String> expectedColumns = entry.getValue();

            assertTrue(actualTables.contains(tableName), "Expected table: " + tableName);

            List<String> actualColumns = jdbi.withHandle(handle ->
                    handle.createQuery("PRAGMA table_info(" + tableName + ")")
                            .map((rs, ctx) -> rs.getString("name"))
                            .list()
            );

            for (String column : expectedColumns) {
                assertTrue(actualColumns.contains(column),
                        "Expected column '" + column + "' in table '" + tableName + "'");
            }
        }
    }
}
