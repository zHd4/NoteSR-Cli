package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.Note;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.dateTimeToString;
import static app.notesr.cli.db.DbUtils.parseDateTime;
import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class NoteDaoTest {
    private static final Faker FAKER = new Faker();
    private static final int TEST_NOTES_COUNT = 5;

    private File dbFile;
    private DbConnection dbConnection;

    private NoteDao noteDao;
    private LinkedHashSet<Note> testNotes;

    @BeforeEach
    public void beforeEach() {
        String dbPath = getTempPath(randomUUID().toString());

        dbFile = new File(dbPath);
        dbConnection = new DbConnection(dbPath);

        noteDao = new NoteDao(dbConnection);
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
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
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

        assertNotNull(actual, "Actual note is null");
        assertEquals(expected, actual, "Notes are different");
    }

    @Test
    public void testGetAll() throws SQLException {
        testNotes.forEach(testNote -> {
            String sql = "INSERT INTO notes (id, name, text, updated_at) VALUES (?, ?, ?, ?)";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setString(1, testNote.getId());
                stmt.setString(2, testNote.getName());
                stmt.setString(3, testNote.getText());
                stmt.setString(4, dateTimeToString(testNote.getUpdatedAt()));

                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        Set<Note> actual = noteDao.getAll();

        assertNotNull(actual, "Actual notes is null");
        assertEquals(testNotes, actual, "Notes are different");
    }

    @AfterEach
    public void afterEach() {
        assertTrue(dbFile.delete());
    }
}
