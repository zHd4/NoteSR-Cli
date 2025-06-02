package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DateTimeUtils.parseDateTime;
import static app.notesr.cli.util.DbUtils.insertNote;
import static app.notesr.cli.util.ModelGenerator.generateTestNotes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NoteEntityDaoTest {
    private static final int TEST_NOTES_COUNT = 5;

    private DbConnection db;
    private NoteEntityDao noteEntityDao;

    private LinkedHashSet<Note> testNotes;

    @BeforeEach
    void setUp() {
        db = new DbConnection(":memory:");

        noteEntityDao = new NoteEntityDao(db);
        testNotes = new LinkedHashSet<>(generateTestNotes(TEST_NOTES_COUNT));
    }

    @Test
    void testAdd() throws SQLException {
        Note expected = testNotes.getFirst();
        Note actual = null;

        noteEntityDao.add(expected);

        String sql = "SELECT * FROM notes WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, expected.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                actual = Note.builder()
                        .id(rs.getString(1))
                        .name(rs.getString(2))
                        .text(rs.getString(3))
                        .updatedAt(parseDateTime(rs.getString(4)))
                        .build();
            }
        }

        assertNotNull(actual, "Actual note must be not null");
        assertEquals(expected, actual, "Notes are different");
    }

    @Test
    void testGetAll() throws SQLException {
        testNotes.forEach(testNote -> insertNote(db.getConnection(), testNote));
        Set<Note> actual = noteEntityDao.getAll();

        assertNotNull(actual, "Actual notes must be not null");
        assertEquals(testNotes, actual, "Notes are different");
    }

    @Test
    void testGetById() throws SQLException {
        for (Note expected : testNotes) {
            insertNote(db.getConnection(), expected);
            Note actual = noteEntityDao.getById(expected.getId());

            assertNotNull(actual, "Actual note must be not null");
            assertEquals(expected, actual, "Notes are different");
        }
    }
}
