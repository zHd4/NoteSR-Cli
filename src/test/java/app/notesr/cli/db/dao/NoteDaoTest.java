package app.notesr.cli.db.dao;

import app.notesr.cli.TestBase;
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

import static app.notesr.cli.db.DbUtils.parseDateTime;
import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NoteDaoTest extends TestBase {
    private static final Faker FAKER = new Faker();

    private File dbFile;
    private DbConnection dbConnection;

    private NoteDao noteDao;
    private Note testNote;

    @BeforeEach
    public void beforeEach() {
        String dbPath = getTempPath(randomUUID().toString());

        dbFile = new File(dbPath);
        dbConnection = new DbConnection(dbPath);

        noteDao = new NoteDao(dbConnection);
        testNote = Note.builder()
                .id(randomUUID().toString())
                .name(FAKER.text().text(5, 15))
                .text(FAKER.text().text())
                .updatedAt(truncateDateTime(LocalDateTime.now()))
                .build();
    }

    @Test
    public void testAdd() throws SQLException {
        noteDao.add(testNote);
        Note actualNote = null;

        String sql = "SELECT * FROM notes WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, testNote.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                actualNote = Note.builder()
                        .id(rs.getString(1))
                        .name(rs.getString(2))
                        .text(rs.getString(3))
                        .updatedAt(parseDateTime(rs.getString(4)))
                        .build();
            }
        }

        assertNotNull(actualNote, "Actual note is null");
        assertEquals(testNote, actualNote, "Notes are different");
    }

    @AfterEach
    public void afterEach() {
        assertTrue(dbFile.delete());
    }
}
