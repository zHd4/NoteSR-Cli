package app.notesr.cli.service;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.FileInfoDtoDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.data.dto.FilesTableRowDto;
import app.notesr.cli.core.exception.NoteNotFoundException;
import app.notesr.cli.data.model.Note;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilesListingServiceTest {

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
        String noteId = randomUUID().toString();
        String fileId = randomUUID().toString();

        FilesTableRowDto dto = new FilesTableRowDto(fileId, "name.txt", 1024L, LocalDateTime.now());

        when(noteEntityDao.getById(noteId)).thenReturn(new Note());
        when(fileInfoDtoDao.getFilesTableRowsByNoteId(noteId)).thenReturn(new LinkedHashSet<>(Set.of(dto)));

        Set<FilesTableRowDto> result = service.listFiles(noteId);

        assertEquals(1, result.size(), "Expected exactly one file to be returned");
        assertTrue(result.contains(dto), "Returned set should contain the expected file DTO");

        verify(noteEntityDao).getById(noteId);
        verify(fileInfoDtoDao).getFilesTableRowsByNoteId(noteId);
    }

    @Test
    void testListFilesThrowsExceptionIfNoteDoesNotExist() {
        String noteId = randomUUID().toString();
        when(noteEntityDao.getById(noteId)).thenReturn(null);

        assertThrows(NoteNotFoundException.class, () -> service.listFiles(noteId),
                "Should throw NoteNotFoundException when the note does not exist");

        verify(noteEntityDao).getById(noteId);
        verify(fileInfoDtoDao, never()).getFilesTableRowsByNoteId(any());
    }
}
