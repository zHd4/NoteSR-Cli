package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.Note;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import static app.notesr.cli.db.DbUtils.dateTimeToString;

@RequiredArgsConstructor
public class NoteDao {
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
}
