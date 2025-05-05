package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.DbUtils;
import net.datafaker.Faker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static app.notesr.cli.db.DbUtils.parseDateTime;
import static app.notesr.cli.db.DbUtils.truncateDateTime;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class FileInfoDaoTest {
    private static final int TEST_FILES_INFOS_COUNT = 5;
    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;

    private static final Faker FAKER = new Faker();
    private static final Random RANDOM = new Random();

    private DbConnection db;
    private FileInfoDao fileInfoDao;

    private Note testNote;
    private LinkedHashSet<FileInfo> testFileInfos;

    @BeforeEach
    public void beforeEach() {
        db = new DbConnection(":memory:");

        fileInfoDao = new FileInfoDao(db);

        testNote = getTestNote();
        testFileInfos = new LinkedHashSet<>();

        for (int i = 0; i < TEST_FILES_INFOS_COUNT; i++) {
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

        DbUtils.insertNote(db.getConnection(), testNote);
    }

    @Test
    public void testAdd() throws SQLException {
        FileInfo expected = testFileInfos.getFirst();
        FileInfo actual = null;

        fileInfoDao.add(expected);

        String sql = "SELECT id, note_id, size, name, created_at, updated_at FROM files_info WHERE id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
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

        assertNotNull(actual, "Actual file info must be not null");
        assertEquals(expected, actual, "Files infos are different");
    }

    @Test
    public void testGetAll() throws SQLException {
        testFileInfos.forEach(fileInfo -> DbUtils.insertFileInfo(db.getConnection(), fileInfo));

        Set<FileInfo> actual = fileInfoDao.getAll();

        assertNotNull(actual, "Actual files infos must be not null");
        assertEquals(testFileInfos, actual, "Files infos are different");
    }

    @Test
    public void testGetByNoteId() throws SQLException {
        testFileInfos.forEach(fileInfo -> DbUtils.insertFileInfo(db.getConnection(), fileInfo));

        Set<FileInfo> actual = fileInfoDao.getByNoteId(testNote.getId());

        assertNotNull(actual, "Actual files infos must be not null");
        assertEquals(testFileInfos, actual, "Files infos are different");

        for (FileInfo fileInfo : actual) {
            assertEquals(testNote.getId(), fileInfo.getNoteId(), "Unexpected note id");
        }

        Note additionalTestNote = getTestNote();
        DbUtils.insertNote(db.getConnection(), additionalTestNote);

        actual = fileInfoDao.getByNoteId(additionalTestNote.getId());

        assertNotNull(actual, "Actual files infos must be not null");
        assertTrue(actual.isEmpty(), "Actual must be empty");
    }

    @Test
    public void testGetById() throws SQLException {
        for (FileInfo expected : testFileInfos) {
            DbUtils.insertFileInfo(db.getConnection(), expected);
            FileInfo actual = fileInfoDao.getById(expected.getId());

            assertNotNull(actual, "Actual file info must be not null");
            assertEquals(expected, actual, "Files infos are different");
        }
    }

    private Note getTestNote() {
        return Note.builder()
                .id(randomUUID().toString())
                .name(FAKER.text().text(5, 15))
                .text(FAKER.text().text())
                .updatedAt(truncateDateTime(LocalDateTime.now()))
                .build();
    }
}
