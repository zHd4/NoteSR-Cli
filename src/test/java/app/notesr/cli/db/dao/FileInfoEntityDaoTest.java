package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.DbUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

import static app.notesr.cli.db.DateTimeUtils.parseDateTime;
import static app.notesr.cli.util.ModelGenerator.generateTestFilesInfos;
import static app.notesr.cli.util.ModelGenerator.generateTestNote;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileInfoEntityDaoTest {
    private static final int TEST_FILES_INFOS_COUNT = 5;
    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;

    private DbConnection db;
    private FileInfoEntityDao fileInfoEntityDao;

    private Note testNote;
    private LinkedHashSet<FileInfo> testFileInfos;

    @BeforeEach
    void setUp() {
        db = new DbConnection(":memory:");

        fileInfoEntityDao = new FileInfoEntityDao(db);

        testNote = generateTestNote();
        testFileInfos = new LinkedHashSet<>(generateTestFilesInfos(testNote, TEST_FILES_INFOS_COUNT,
                MIN_FILE_SIZE, MAX_FILE_SIZE));

        DbUtils.insertNote(db.getConnection(), testNote);
    }

    @Test
    void testAdd() throws SQLException {
        FileInfo expected = testFileInfos.getFirst();
        FileInfo actual = null;

        fileInfoEntityDao.add(expected);

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
    void testGetAll() throws SQLException {
        testFileInfos.forEach(fileInfo -> DbUtils.insertFileInfo(db.getConnection(), fileInfo));

        Set<FileInfo> actual = fileInfoEntityDao.getAll();

        assertNotNull(actual, "Actual files infos must be not null");
        assertEquals(testFileInfos, actual, "Files infos are different");
    }

    @Test
    void testGetCountByNoteId() throws SQLException {
        testFileInfos.forEach(fileInfo -> DbUtils.insertFileInfo(db.getConnection(), fileInfo));

        Long expected = (long) testFileInfos.size();
        Long actual = fileInfoEntityDao.getCountByNoteId(testNote.getId());

        assertEquals(expected, actual, "Unexpected files infos count");
    }

    @Test
    void testGetByNoteId() throws SQLException {
        Note additionalTestNote = generateTestNote();

        testFileInfos.forEach(fileInfo -> DbUtils.insertFileInfo(db.getConnection(), fileInfo));
        DbUtils.insertNote(db.getConnection(), additionalTestNote);

        Set<FileInfo> actual = fileInfoEntityDao.getByNoteId(testNote.getId());

        assertNotNull(actual, "Actual files infos must be not null");
        assertFalse(actual.isEmpty(), "Actual must be not empty");

        for (FileInfo fileInfo : actual) {
            assertEquals(testNote.getId(), fileInfo.getNoteId(), "Unexpected note id");
        }
    }

    @Test
    void testGetById() throws SQLException {
        for (FileInfo expected : testFileInfos) {
            DbUtils.insertFileInfo(db.getConnection(), expected);
            FileInfo actual = fileInfoEntityDao.getById(expected.getId());

            assertNotNull(actual, "Actual file info must be not null");
            assertEquals(expected, actual, "Files infos are different");
        }
    }
}
