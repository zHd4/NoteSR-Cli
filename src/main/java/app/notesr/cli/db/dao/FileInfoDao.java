package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.FileInfo;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.dateTimeToString;
import static app.notesr.cli.db.DbUtils.parseDateTime;

@RequiredArgsConstructor
public final class FileInfoDao {
    private final DbConnection db;

    public void add(FileInfo fileInfo) throws SQLException {
        String sql = "INSERT INTO files_info (id, note_id, size, name, type, thumbnail, created_at, updated_at)"
                + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
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

    public Set<FileInfo> getAll() throws SQLException {
        Set<FileInfo> results = new LinkedHashSet<>();
        String sql = "SELECT * FROM files_info";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FileInfo fileInfo = FileInfo.builder()
                        .id(rs.getString(1))
                        .noteId(rs.getString(2))
                        .name(rs.getString(3))
                        .type(rs.getString(4))
                        .thumbnail(rs.getBytes(5))
                        .size(rs.getLong(6))
                        .createdAt(parseDateTime(rs.getString(7)))
                        .updatedAt(parseDateTime(rs.getString(8)))
                        .build();

                results.add(fileInfo);
            }
        }

        return results;
    }

    public Set<FileInfo> getByNoteId(String noteId) throws SQLException {
        Set<FileInfo> results = new LinkedHashSet<>();
        String sql = "SELECT * FROM files_info WHERE note_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, noteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FileInfo fileInfo = FileInfo.builder()
                        .id(rs.getString(1))
                        .noteId(rs.getString(2))
                        .name(rs.getString(3))
                        .type(rs.getString(4))
                        .thumbnail(rs.getBytes(5))
                        .size(rs.getLong(6))
                        .createdAt(parseDateTime(rs.getString(7)))
                        .updatedAt(parseDateTime(rs.getString(8)))
                        .build();

                results.add(fileInfo);
            }
        }

        return results;
    }

    public Long getCountByNoteId(String noteId) throws SQLException {
        Long count = null;
        String sql = "SELECT COUNT(*) FROM files_info WHERE note_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, noteId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getLong(1);
            }
        }

        return count;
    }

    public FileInfo getById(String id) throws SQLException {
        String sql = "SELECT * FROM files_info WHERE id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return FileInfo.builder()
                        .id(rs.getString(1))
                        .noteId(rs.getString(2))
                        .name(rs.getString(3))
                        .type(rs.getString(4))
                        .thumbnail(rs.getBytes(5))
                        .size(rs.getLong(6))
                        .createdAt(parseDateTime(rs.getString(7)))
                        .updatedAt(parseDateTime(rs.getString(8)))
                        .build();
            }
        }

        return null;
    }
}
