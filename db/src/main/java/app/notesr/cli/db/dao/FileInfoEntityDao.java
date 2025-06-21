package app.notesr.cli.db.dao;

import app.notesr.cli.model.FileInfo;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Set;

@RegisterBeanMapper(FileInfo.class)
public interface FileInfoEntityDao {

    @SqlUpdate("""
        INSERT INTO files_info (
            id, note_id, size, name, type, thumbnail, created_at, updated_at
        ) VALUES (
            :id, :noteId, :size, :name, :type, :thumbnail, :createdAt, :updatedAt
        )
    """)
    void add(@BindBean FileInfo fileInfo);

    @SqlQuery("SELECT * FROM files_info")
    Set<FileInfo> getAll();

    @SqlQuery("SELECT * FROM files_info WHERE note_id = :noteId")
    Set<FileInfo> getByNoteId(@Bind("noteId") String noteId);

    @SqlQuery("SELECT COUNT(*) FROM files_info WHERE note_id = :noteId")
    Long getCountByNoteId(@Bind("noteId") String noteId);

    @SqlQuery("SELECT * FROM files_info WHERE id = :id")
    FileInfo getById(@Bind("id") String id);
}
