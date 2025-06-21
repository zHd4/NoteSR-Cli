package app.notesr.cli.db.dao;

import app.notesr.cli.dto.NotesTableRowDto;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Set;

@RegisterBeanMapper(NotesTableRowDto.class)
public interface NoteFileInfoDtoDao {

    @SqlQuery("""
        SELECT
            n.id AS note_id,
            SUBSTR(n.name, 1, 30) AS note_short_name,
            SUBSTR(n.text, 1, 30) AS note_short_text,
            n.updated_at AS note_updated_at,
            (
                SELECT COUNT(*)
                FROM files_info f
                WHERE f.note_id = n.id
            ) AS attached_files_count
        FROM notes n
        ORDER BY n.updated_at DESC
    """)
    Set<NotesTableRowDto> getNotesTable();
}
