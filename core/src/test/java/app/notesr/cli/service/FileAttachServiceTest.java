package app.notesr.cli.service;


import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Random;
import java.util.Set;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileAttachServiceTest {
    private static final Random RANDOM = new Random();

    private static final String BLANK_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final int MAX_TEST_FILE_SIZE = 1024 * 100;

    @TempDir
    private Path tempDir;

    private DbConnection db;
    private FileAttachService fileAttachService;

    @BeforeEach
    void setUp() {
        Path dbPath = getFixturePath("backup.db");

        db = new DbConnection(dbPath.toString());
        fileAttachService = new FileAttachService(db);
    }

    @Test
    void testAttachFile() throws IOException, SQLException, NoteNotFoundException {
        File testFile = createTestFile();
        Note testNote = getTestNote();

        fileAttachService.attachFile(testFile, testNote.getId());
        assertFileAttached(testNote, testFile);
    }

    @Test
    void testAttachFileWhenNoteNotExist() throws IOException {
        File testFile = createTestFile();
        assertThrows(NoteNotFoundException.class, () -> fileAttachService.attachFile(testFile, BLANK_UUID));
    }

    private void assertFileAttached(Note note, File file) throws SQLException, IOException {
        String expectedFileName = file.getName();
        long expectedFileSize = Files.size(file.toPath());

        FileInfoEntityDao fileInfoEntityDao = new FileInfoEntityDao(db);
        Set<FileInfo> attachedFilesInfos = fileInfoEntityDao.getByNoteId(note.getId());

        FileInfo actualFileInfo = attachedFilesInfos.stream()
                .filter(fileInfo -> expectedFileName.equals(fileInfo.getName()))
                .findAny()
                .orElse(null);

        assertNotNull(actualFileInfo, "File " + expectedFileName + " not found");
        assertEquals(expectedFileSize, actualFileInfo.getSize(), "Unexpected file size");
    }

    private File createTestFile() throws IOException {
        File testFile = tempDir.resolve("test_file_" + randomUUID()).toFile();
        byte[] testFileData = new byte[RANDOM.nextInt(1, MAX_TEST_FILE_SIZE)];

        RANDOM.nextBytes(testFileData);
        Files.write(testFile.toPath(), testFileData);

        return testFile;
    }

    private Note getTestNote() throws SQLException {
        NoteEntityDao noteEntityDao = new NoteEntityDao(db);
        return noteEntityDao.getAll().stream().findFirst().orElseThrow();
    }
}
