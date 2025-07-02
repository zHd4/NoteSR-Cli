package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.FileInfoDtoDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.dto.FilesTableRowDto;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.model.Note;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilesListingServiceTest {
    private static final String TEST_NOTE_ID = "note-123";

    @Mock
    private DbConnection db;

    @Mock
    private Jdbi jdbi;

    @Mock
    private NoteEntityDao noteEntityDao;

    @Mock
    private FileInfoDtoDao fileInfoDtoDao;

    private FilesListingService service;

    @BeforeEach
    void setUp() {
        when(db.getConnection()).thenReturn(jdbi);
        when(jdbi.onDemand(NoteEntityDao.class)).thenReturn(noteEntityDao);
        when(jdbi.onDemand(FileInfoDtoDao.class)).thenReturn(fileInfoDtoDao);

        service = new FilesListingService(db);
    }

    @Test
    void testListFilesReturnsFileListForExistingNote() throws Exception {
        FilesTableRowDto dto = new FilesTableRowDto("file-1", "name.txt", 1024L, LocalDateTime.now());

        when(noteEntityDao.getById(TEST_NOTE_ID)).thenReturn(new Note());
        when(fileInfoDtoDao.getFilesTableRowsByNoteId(TEST_NOTE_ID)).thenReturn(new LinkedHashSet<>(Set.of(dto)));

        Set<FilesTableRowDto> result = service.listFiles(TEST_NOTE_ID);

        assertEquals(1, result.size(), "Expected exactly one file to be returned");
        assertTrue(result.contains(dto), "Returned set should contain the expected file DTO");

        verify(noteEntityDao).getById(TEST_NOTE_ID);
        verify(fileInfoDtoDao).getFilesTableRowsByNoteId(TEST_NOTE_ID);
    }

    @Test
    void testListFilesThrowsExceptionIfNoteDoesNotExist() {
        when(noteEntityDao.getById(TEST_NOTE_ID)).thenReturn(null);

        assertThrows(NoteNotFoundException.class, () -> service.listFiles(TEST_NOTE_ID),
                "Should throw NoteNotFoundException when the note does not exist");

        verify(noteEntityDao).getById(TEST_NOTE_ID);
        verify(fileInfoDtoDao, never()).getFilesTableRowsByNoteId(any());
    }
}
