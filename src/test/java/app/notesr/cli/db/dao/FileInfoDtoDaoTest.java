package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.FilesTableRowDto;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.DbUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static app.notesr.cli.util.ModelGenerator.generateTestFilesInfos;
import static app.notesr.cli.util.ModelGenerator.generateTestNote;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FileInfoDtoDaoTest {
    private static final int TEST_FILES_INFOS_COUNT = 5;
    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;

    private DbConnection db;
    private FileInfoDtoDao fileInfoDtoDao;

    private Note testNote;
    private LinkedHashSet<FileInfo> testFileInfos;

    @BeforeEach
    void setUp() {
        db = new DbConnection(":memory:");
        fileInfoDtoDao = new FileInfoDtoDao(db);

        testNote = generateTestNote();
        testFileInfos = new LinkedHashSet<>(generateTestFilesInfos(testNote, TEST_FILES_INFOS_COUNT,
                MIN_FILE_SIZE, MAX_FILE_SIZE));

        DbUtils.insertNote(db.getConnection(), testNote);
    }

    @Test
    void testGetFilesTableRowsByNoteId() throws SQLException {
        Note additionalTestNote = generateTestNote();

        testFileInfos.forEach(fileInfo -> DbUtils.insertFileInfo(db.getConnection(), fileInfo));
        DbUtils.insertNote(db.getConnection(), additionalTestNote);

        Set<FilesTableRowDto> expected = testFileInfos.stream()
                .map(fileInfo -> FilesTableRowDto.builder()
                        .id(fileInfo.getId())
                        .fileName(fileInfo.getName())
                        .fileSize(fileInfo.getSize())
                        .createdAt(fileInfo.getCreatedAt())
                        .updatedAt(fileInfo.getUpdatedAt())
                        .build())
                .collect(Collectors.toSet());

        Set<FilesTableRowDto> actual = fileInfoDtoDao.getFilesTableRowsByNoteId(testNote.getId());

        assertNotNull(actual, "Actual files infos must be not null");
        assertFalse(actual.isEmpty(), "Actual must be not empty");

        assertEquals(expected, actual, "DTOs are different");
    }
}
