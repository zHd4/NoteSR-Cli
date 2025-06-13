package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.Note;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.util.DateTimeUtils.dateTimeToString;
import static app.notesr.cli.util.DateTimeUtils.parseDateTime;

@RequiredArgsConstructor
public final class NoteEntityDao {
    private final DbConnection db;

    public void add(Note note) throws SQLException {
        String sql = "INSERT INTO notes (id, name, text, updated_at) VALUES (?, ?, ?, ?)";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, note.getId());
            stmt.setString(2, note.getName());
            stmt.setString(3, note.getText());
            stmt.setString(4, dateTimeToString(note.getUpdatedAt()));

            stmt.executeUpdate();
        }
    }

    public Set<Note> getAll() throws SQLException {
        Set<Note> results = new LinkedHashSet<>();

        String sql = "SELECT * "
                + "FROM notes;";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Note note = Note.builder()
                        .id(rs.getString(1))
                        .name(rs.getString(2))
                        .text(rs.getString(3))
                        .updatedAt(parseDateTime(rs.getString(4)))
                        .build();

                results.add(note);
            }
        }

        return results;
    }

    public Note getById(String id) throws SQLException {
        String sql = "SELECT * "
                + "FROM notes WHERE id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Note.builder()
                        .id(rs.getString(1))
                        .name(rs.getString(2))
                        .text(rs.getString(3))
                        .updatedAt(parseDateTime(rs.getString(4)))
                        .build();
            }
        }

        return null;
    }
}
