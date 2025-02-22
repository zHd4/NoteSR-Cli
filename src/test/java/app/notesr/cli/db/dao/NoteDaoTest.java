package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.Note;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.parseDateTime;
import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static app.notesr.cli.util.FixtureUtils.insertNote;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class NoteDaoTest {
    private static final Faker FAKER = new Faker();
    private static final int TEST_NOTES_COUNT = 5;

    private DbConnection db;
    private NoteDao noteDao;

    private LinkedHashSet<Note> testNotes;

    @BeforeEach
    public void beforeEach() {
        db = new DbConnection(":memory:");

        noteDao = new NoteDao(db);
        testNotes = new LinkedHashSet<>();

        for (int i = 0; i < TEST_NOTES_COUNT; i++) {
            Note testNote = Note.builder()
                    .id(randomUUID().toString())
                    .name(FAKER.text().text(5, 15))
                    .text(FAKER.text().text())
                    .updatedAt(truncateDateTime(LocalDateTime.now()))
                    .build();

            testNotes.add(testNote);
        }
    }

    @Test
    public void testAdd() throws SQLException {
        Note expected = testNotes.getFirst();
        Note actual = null;

        noteDao.add(expected);

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

        assertNotNull(actual, "Actual note must not be null");
        assertEquals(expected, actual, "Notes are different");
    }

    @Test
    public void testGetAll() throws SQLException {
        insertTestNotes();
        Set<Note> actual = noteDao.getAll();

        assertNotNull(actual, "Actual notes must not be null");
        assertEquals(testNotes, actual, "Notes are different");
    }

    @Test
    public void testGetById() throws SQLException {
        insertTestNotes();

        Note firstExpected = testNotes.getFirst();
        Note lastExpected = testNotes.getLast();

        Note firstActual = noteDao.getById(firstExpected.getId());
        Note lastActual = noteDao.getById(lastExpected.getId());

        assertNotNull(firstActual, "Actual note must not be null");
        assertNotNull(lastActual, "Actual note must not be null");

        assertEquals(firstExpected, firstActual, "Notes are different");
        assertEquals(lastExpected, lastActual, "Notes are different");
    }

    private void insertTestNotes() {
        testNotes.forEach(testNote -> insertNote(db.getConnection(), testNote));
    }
}
