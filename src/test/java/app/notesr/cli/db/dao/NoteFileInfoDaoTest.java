package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.NotesTableDto;
import app.notesr.cli.model.FileInfo;
import app.notesr.cli.model.Note;
import app.notesr.cli.util.DbUtils;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import app.notesr.cli.util.ModelGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NoteFileInfoDaoTest {
    private static final int TEST_NOTES_COUNT = 5;
    private static final int TEST_FILES_COUNT = 5;

    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;

    private static final int VALUE_CROP_INDEX = 30;

    @Test
    void testGetNoteFileInfoOutputTable() throws SQLException {
        DbConnection db = new DbConnection(":memory:");
        NoteFileInfoDao noteFileInfoDao = new NoteFileInfoDao(db);

        List<NotesTableDto> notesTableDtos = new LinkedList<>();

        for (Note testNote : ModelGenerator.generateTestNotes(TEST_NOTES_COUNT)) {
            Set<FileInfo> testFilesInfos = ModelGenerator.generateTestFilesInfos(testNote, TEST_FILES_COUNT,
                    MIN_FILE_SIZE, MAX_FILE_SIZE);

            DbUtils.insertNote(db.getConnection(), testNote);
            testFilesInfos.forEach(testFileInfo -> DbUtils.insertFileInfo(db.getConnection(), testFileInfo));

            NotesTableDto notesTableDto = NotesTableDto.builder()
                    .noteId(testNote.getId())
                    .noteShortName(cropValue(testNote.getName()))
                    .noteShortText(cropValue(testNote.getText()))
                    .noteUpdatedAt(testNote.getUpdatedAt())
                    .attachedFilesCount((long) testFilesInfos.size())
                    .build();

            notesTableDtos.add(notesTableDto);
        }

        Set<NotesTableDto> expected = notesTableDtos.stream()
                .sorted(Comparator.comparing(NotesTableDto::getNoteId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<NotesTableDto> actual = noteFileInfoDao.getNoteFileInfoOutputTable().stream()
                .sorted(Comparator.comparing(NotesTableDto::getNoteId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        assertFalse(actual.isEmpty(), "The Set must contain DTOs");
        assertEquals(expected, actual, "Set of DTO's are different");
    }

    private String cropValue(String value) {
        if (value.length() <= VALUE_CROP_INDEX) {
            return value;
        }

        return value.substring(0, VALUE_CROP_INDEX);
    }
}
