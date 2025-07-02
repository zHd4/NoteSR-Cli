package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.FileInfoEntityDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.dto.NoteOutputDto;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.model.Note;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteReadingServiceTest {

    @Mock
    private DbConnection db;

    @Mock
    private Jdbi jdbi;

    @Mock
    private NoteEntityDao noteEntityDao;

    @Mock
    private FileInfoEntityDao fileInfoEntityDao;

    private NoteReadingService service;

    @BeforeEach
    void setUp() {
        when(db.getConnection()).thenReturn(jdbi);
        when(jdbi.onDemand(NoteEntityDao.class)).thenReturn(noteEntityDao);
        when(jdbi.onDemand(FileInfoEntityDao.class)).thenReturn(fileInfoEntityDao);

        service = new NoteReadingService(db);
    }

    @Test
    void testReadNote() throws Exception {
        String noteId = randomUUID().toString();
        Note note = new Note(noteId, "Test note", "Body", LocalDateTime.now());
        Long attachmentsCount = 5L;

        when(noteEntityDao.getById(noteId)).thenReturn(note);
        when(fileInfoEntityDao.getCountByNoteId(noteId)).thenReturn(attachmentsCount);

        NoteOutputDto actual = service.readNote(noteId);

        assertNotNull(actual, "Returned DTO should not be null");
        assertEquals(note, actual.getNote(), "Note in DTO should match the one returned by DAO");
        assertEquals(attachmentsCount, actual.getAttachmentsCount(), "Attachment count should match DAO");

        verify(noteEntityDao).getById(noteId);
        verify(fileInfoEntityDao).getCountByNoteId(noteId);
    }

    @Test
    void testReadNoteWhenNoteNotFound() {
        String noteId = randomUUID().toString();
        when(noteEntityDao.getById(noteId)).thenReturn(null);

        NoteNotFoundException exception = assertThrows(NoteNotFoundException.class, () ->
                service.readNote(noteId), "Expected exception if note not found");

        assertEquals(noteId, exception.getNoteId(), "Exception should contain the correct note ID");

        verify(noteEntityDao).getById(noteId);
        verify(fileInfoEntityDao).getCountByNoteId(any());
    }
}
