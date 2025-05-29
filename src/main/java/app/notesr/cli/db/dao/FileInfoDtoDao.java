package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.FilesTableRowDto;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.parseDateTime;

@RequiredArgsConstructor
public class FileInfoDtoDao {
    private final DbConnection db;

    public Set<FilesTableRowDto> getFilesTableRowsByNoteId(String noteId) throws SQLException {
        Set<FilesTableRowDto> results = new LinkedHashSet<>();
        String sql = "SELECT id, name, size, created_at, updated_at FROM files_info WHERE note_id = ?";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, noteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FilesTableRowDto fileInfo = FilesTableRowDto.builder()
                        .id(rs.getString(1))
                        .fileName(rs.getString(2))
                        .fileSize(rs.getLong(3))
                        .createdAt(parseDateTime(rs.getString(4)))
                        .updatedAt(parseDateTime(rs.getString(5)))
                        .build();

                results.add(fileInfo);
            }
        }

        return results;
    }
}
