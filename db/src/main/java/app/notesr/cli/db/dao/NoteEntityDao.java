package app.notesr.cli.db.dao;

import app.notesr.cli.model.Note;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Set;

@RegisterBeanMapper(Note.class)
public interface NoteEntityDao {
    @SqlUpdate("INSERT INTO notes (id, name, text, updated_at) VALUES (:id, :name, :text, :updatedAt)")
    void add(@BindBean Note note);

    @SqlQuery("SELECT * FROM notes")
    Set<Note> getAll();

    @SqlQuery("SELECT * FROM notes WHERE id = :id")
    Note getById(@Bind("id") String id);
}
