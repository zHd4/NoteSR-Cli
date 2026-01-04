/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.data.dao;

import app.notesr.cli.data.model.Note;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Set;

@RegisterBeanMapper(Note.class)
public interface NoteEntityDao {
    @SqlUpdate("INSERT INTO notes (id, name, text, created_at, updated_at)"
            + " VALUES (:id, :name, :text, :createdAt, :updatedAt)")
    void add(@BindBean Note note);

    @SqlUpdate("UPDATE notes SET name = :name, text = :text, created_at = :createdAt, updated_at = :updatedAt"
            + " WHERE id = :id")
    void update(@BindBean Note note);

    @SqlQuery("SELECT * FROM notes")
    Set<Note> getAll();

    @SqlQuery("SELECT * FROM notes WHERE id = :id")
    Note getById(@Bind("id") String id);
}
