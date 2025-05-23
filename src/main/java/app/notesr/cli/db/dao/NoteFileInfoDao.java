package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.NoteFileInfoOutputDto;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.parseDateTime;

@RequiredArgsConstructor
public class NoteFileInfoDao {
    private static final String GET_NOTE_FILE_INFO_OUTPUT_TABLE_QUERY = """
                SELECT
                	n.id,
                	SUBSTR(n.name, 1, 30),
                	SUBSTR(n.text, 1, 30),
                	n.updated_at,
                	(
                		SELECT COUNT(*)
                		FROM files_info f
                		WHERE f.note_id = n.id
                	) AS attached_files_count
                FROM notes n
                ORDER BY id;
                """;

    private final DbConnection db;

    public Set<NoteFileInfoOutputDto> getNoteFileInfoOutputTable() throws SQLException {
        Set<NoteFileInfoOutputDto> results = new LinkedHashSet<>();

        try (PreparedStatement stmt = db.getConnection().prepareStatement(GET_NOTE_FILE_INFO_OUTPUT_TABLE_QUERY)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                NoteFileInfoOutputDto noteFileInfoOutputDto = NoteFileInfoOutputDto.builder()
                        .noteId(rs.getString(1))
                        .noteShortName(rs.getString(2))
                        .noteShortText(rs.getString(3))
                        .noteUpdatedAt(parseDateTime(rs.getString(4)))
                        .attachedFilesCount(rs.getLong(5))
                        .build();

                results.add(noteFileInfoOutputDto);
            }
        }

        return results;
    }
}
