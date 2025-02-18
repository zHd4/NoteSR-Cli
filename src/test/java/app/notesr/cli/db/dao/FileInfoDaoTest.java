package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.FileInfo;
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
import java.util.Random;

import static app.notesr.cli.db.DbUtils.dateTimeToString;
import static app.notesr.cli.db.DbUtils.parseDateTime;
import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileInfoDaoTest {
    private static final int TEST_FILE_INFOS_COUNT = 5;
    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();

    private File dbFile;
    private DbConnection dbConnection;

    private FileInfoDao fileInfoDao;

    private Note testNote;
    private LinkedHashSet<FileInfo> testFileInfos;

    @BeforeEach
    public void beforeEach() {
        String dbPath = getTempPath(randomUUID().toString());

        dbFile = new File(dbPath);
        dbConnection = new DbConnection(dbPath);

        fileInfoDao = new FileInfoDao(dbConnection);

        testNote = getTestNote();
        testFileInfos = new LinkedHashSet<>();

        for (int i = 0; i < TEST_FILE_INFOS_COUNT; i++) {
            FileInfo fileInfo = FileInfo.builder()
                    .id(randomUUID().toString())
                    .noteId(testNote.getId())
                    .size(RANDOM.nextLong(MIN_FILE_SIZE, MAX_FILE_SIZE))
                    .name(FAKER.text().text(5, 15))
                    .createdAt(truncateDateTime(LocalDateTime.now()))
                    .updatedAt(truncateDateTime(LocalDateTime.now()))
                    .build();

            testFileInfos.add(fileInfo);
        }

        insertTestNote();
    }

    @Test
    public void testAdd() throws SQLException {
        FileInfo expected = testFileInfos.getFirst();
        FileInfo actual = null;

        fileInfoDao.add(expected);

        String sql = "SELECT id, noteId, size, name, created_at, updated_at FROM files_info WHERE id = ?";
        try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, expected.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                actual = FileInfo.builder()
                        .id(rs.getString(1))
                        .noteId(rs.getString(2))
                        .size(rs.getLong(3))
                        .name(rs.getString(4))
                        .createdAt(parseDateTime(rs.getString(5)))
                        .updatedAt(parseDateTime(rs.getString(6)))
                        .build();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        assertNotNull(actual, "Actual file info is null");
        assertEquals(expected, actual, "Files infos are different");
    }

    @AfterEach
    public void afterEach() {
        assertTrue(dbFile.delete());
    }

    private Note getTestNote() {
        return Note.builder()
                .id(randomUUID().toString())
                .name(FAKER.text().text(5, 15))
                .text(FAKER.text().text())
                .updatedAt(truncateDateTime(LocalDateTime.now()))
                .build();
    }

    private void insertTestNote() {
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
    }

    private void insertTestFileInfos() {
        testFileInfos.forEach(testFileInfo -> {
            String sql = "INSERT INTO files_info (id, note_id, size, name, created_at, updated_at)" +
                    " VALUES (?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = dbConnection.getConnection().prepareStatement(sql)) {
                stmt.setString(1, testFileInfo.getId());
                stmt.setString(2, testFileInfo.getNoteId());
                stmt.setLong(3, testFileInfo.getSize());
                stmt.setString(4, testFileInfo.getName());
                stmt.setString(5, dateTimeToString(testFileInfo.getCreatedAt()));
                stmt.setString(6, dateTimeToString(testFileInfo.getUpdatedAt()));

                stmt.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
