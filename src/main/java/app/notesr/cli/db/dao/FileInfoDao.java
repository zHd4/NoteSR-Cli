package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.FileInfo;
import lombok.RequiredArgsConstructor;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static app.notesr.cli.db.DbUtils.dateTimeToString;

@RequiredArgsConstructor
public class FileInfoDao {
    private final DbConnection dbConnection;

    public void add(FileInfo fileInfo) throws SQLException {
        String sql = "INSERT INTO files_info (id, note_id, size, name, type, thumbnail, created_at, updated_at)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, fileInfo.getId());
            stmt.setString(2, fileInfo.getNoteId());
            stmt.setLong(3, fileInfo.getSize());
            stmt.setString(4, fileInfo.getName());
            stmt.setString(5, fileInfo.getType());
            stmt.setBytes(6, fileInfo.getThumbnail());
            stmt.setString(7, dateTimeToString(fileInfo.getCreatedAt()));
            stmt.setString(8, dateTimeToString(fileInfo.getUpdatedAt()));

            stmt.executeUpdate();
        }
    }
}
