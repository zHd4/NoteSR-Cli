/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.util.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DbUtils {
    public static String serializeTableAsJson(Jdbi jdbi, String tableName) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String sql = "SELECT * FROM " + tableName;

        List<Map<String, Object>> rows = jdbi.withHandle(handle ->
                handle.createQuery(sql)
                        .mapToMap()
                        .list()
        );

        return objectMapper.writeValueAsString(rows);
    }
}
