package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.FilesTableRowDto;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.util.DateTimeUtils.parseDateTime;

@RequiredArgsConstructor
public final class FileInfoDtoDao {
    private final DbConnection db;

    public Set<FilesTableRowDto> getFilesTableRowsByNoteId(String noteId) throws SQLException {
        Set<FilesTableRowDto> results = new LinkedHashSet<>();
        String sql = "SELECT id, name, size, updated_at FROM files_info WHERE note_id = ? ORDER BY updated_at DESC;";

        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, noteId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                FilesTableRowDto fileInfo = FilesTableRowDto.builder()
                        .id(rs.getString(1))
                        .fileName(rs.getString(2))
                        .fileSize(rs.getLong(3))
                        .updatedAt(parseDateTime(rs.getString(4)))
                        .build();

                results.add(fileInfo);
            }
        }

        return results;
    }
}
