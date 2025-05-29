package app.notesr.cli.db.dao;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.dto.NotesTableRowDto;
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

class NoteFileInfoDtoDaoTest {
    private static final int TEST_NOTES_COUNT = 5;
    private static final int TEST_FILES_COUNT = 5;

    private static final long MIN_FILE_SIZE = 1024;
    private static final long MAX_FILE_SIZE = 1024 * 10;

    private static final int VALUE_CROP_INDEX = 30;

    @Test
    void testGetNotesTable() throws SQLException {
        DbConnection db = new DbConnection(":memory:");
        NoteFileInfoDtoDao noteFileInfoDtoDao = new NoteFileInfoDtoDao(db);

        List<NotesTableRowDto> notesTableRowDtos = new LinkedList<>();

        for (Note testNote : ModelGenerator.generateTestNotes(TEST_NOTES_COUNT)) {
            Set<FileInfo> testFilesInfos = ModelGenerator.generateTestFilesInfos(testNote, TEST_FILES_COUNT,
                    MIN_FILE_SIZE, MAX_FILE_SIZE);

            DbUtils.insertNote(db.getConnection(), testNote);
            testFilesInfos.forEach(testFileInfo -> DbUtils.insertFileInfo(db.getConnection(), testFileInfo));

            NotesTableRowDto notesTableRowDto = NotesTableRowDto.builder()
                    .noteId(testNote.getId())
                    .noteShortName(cropValue(testNote.getName()))
                    .noteShortText(cropValue(testNote.getText()))
                    .noteUpdatedAt(testNote.getUpdatedAt())
                    .attachedFilesCount((long) testFilesInfos.size())
                    .build();

            notesTableRowDtos.add(notesTableRowDto);
        }

        Set<NotesTableRowDto> expected = notesTableRowDtos.stream()
                .sorted(Comparator.comparing(NotesTableRowDto::getNoteId))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        Set<NotesTableRowDto> actual = noteFileInfoDtoDao.getNotesTable().stream()
                .sorted(Comparator.comparing(NotesTableRowDto::getNoteId))
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
