/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.data.dao;

import app.notesr.cli.data.dto.FilesTableRowDto;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.LinkedHashSet;

@RegisterBeanMapper(FilesTableRowDto.class)
public interface FileInfoDtoDao {

    @SqlQuery("""
        SELECT id, name AS file_name, size AS file_size, updated_at
        FROM files_info
        WHERE note_id = :noteId
        ORDER BY updated_at DESC
        """)
    LinkedHashSet<FilesTableRowDto> getFilesTableRowsByNoteId(@Bind("noteId") String noteId);
}
