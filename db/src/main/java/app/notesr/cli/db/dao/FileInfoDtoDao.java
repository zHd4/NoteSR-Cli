package app.notesr.cli.db.dao;

import app.notesr.cli.dto.FilesTableRowDto;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Set;

@RegisterBeanMapper(FilesTableRowDto.class)
public interface FileInfoDtoDao {

    @SqlQuery("""
        SELECT id, name AS file_name, size AS file_size, updated_at
        FROM files_info
        WHERE note_id = :noteId
        ORDER BY updated_at DESC
        """)
    Set<FilesTableRowDto> getFilesTableRowsByNoteId(@Bind("noteId") String noteId);
}
