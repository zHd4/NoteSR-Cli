/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.data.mapper;

import app.notesr.cli.data.model.FileInfo;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public final class FilesInfosJsonMapper extends JsonMapper<FileInfo> {
    @Override
    public List<FileInfo> map(String json) throws IOException {
        List<Map<String, Object>> maps = parseJson(json);
        return maps.stream()
                .map(line -> FileInfo.builder()
                        .id((String) line.get("id"))
                        .noteId((String) line.get("note_id"))
                        .name((String) line.get("name"))
                        .size((Long) line.get("type"))
                        .createdAt(parseDateTime((String) line.get("created_at")))
                        .updatedAt(parseDateTime((String) line.get("updated_at")))
                        .build())
                .toList();
    }
}
