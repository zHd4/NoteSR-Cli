package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.NotesTableRowDto;
import lombok.RequiredArgsConstructor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.parseDateTime;

@RequiredArgsConstructor
public final class NoteFileInfoDtoDao {
    private static final String GET_NOTE_FILE_INFO_OUTPUT_TABLE_QUERY = """
            SELECT
            \tn.id,
            \tSUBSTR(n.name, 1, 30),
            \tSUBSTR(n.text, 1, 30),
            \tn.updated_at,
            \t(
            \t\tSELECT COUNT(*)
            \t\tFROM files_info f
            \t\tWHERE f.note_id = n.id
            \t) AS attached_files_count
            FROM notes n
            ORDER BY n.updated_at DESC;""";

    private final DbConnection db;

    public Set<NotesTableRowDto> getNotesTable() throws SQLException {
        Set<NotesTableRowDto> results = new LinkedHashSet<>();

        try (PreparedStatement stmt = db.getConnection().prepareStatement(GET_NOTE_FILE_INFO_OUTPUT_TABLE_QUERY)) {
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                NotesTableRowDto notesTableRowDto = NotesTableRowDto.builder()
                        .noteId(rs.getString(1))
                        .noteShortName(rs.getString(2))
                        .noteShortText(rs.getString(3))
                        .noteUpdatedAt(parseDateTime(rs.getString(4)))
                        .attachedFilesCount(rs.getLong(5))
                        .build();

                results.add(notesTableRowDto);
            }
        }

        return results;
    }
}
