package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteFileInfoDtoDao;
import app.notesr.cli.dto.NotesTableRowDto;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotesListingServiceTest {
    private NotesListingService service;
    private NoteFileInfoDtoDao noteFileInfoDtoDao;

    @BeforeEach
    void setUp() {
        Jdbi jdbi = mock(Jdbi.class);
        DbConnection db = mock(DbConnection.class);

        noteFileInfoDtoDao = mock(NoteFileInfoDtoDao.class);

        when(db.getConnection()).thenReturn(jdbi);
        when(db.getConnection().onDemand(NoteFileInfoDtoDao.class)).thenReturn(noteFileInfoDtoDao);

        service = new NotesListingService(db);
    }

    @Test
    void testListNotesReturnsExpectedNotes() {
        NotesTableRowDto note1 = NotesTableRowDto.builder()
                .noteId(randomUUID().toString())
                .noteShortName("First")
                .noteShortText("Short text 1")
                .noteUpdatedAt(LocalDateTime.now())
                .attachedFilesCount(2L)
                .build();

        NotesTableRowDto note2 = NotesTableRowDto.builder()
                .noteId(randomUUID().toString())
                .noteShortName("Second")
                .noteShortText("Short text 2")
                .noteUpdatedAt(LocalDateTime.now().minusDays(1))
                .attachedFilesCount(0L)
                .build();

        LinkedHashSet<NotesTableRowDto> mockResult = new LinkedHashSet<>();
        mockResult.add(note1);
        mockResult.add(note2);

        when(noteFileInfoDtoDao.getNotesTable()).thenReturn(mockResult);

        Set<NotesTableRowDto> result = service.listNotes();

        assertEquals(2, result.size(), "Should return exactly two notes");
        assertTrue(result.contains(note1), "Result should contain the first note");
        assertTrue(result.contains(note2), "Result should contain the second note");

        verify(noteFileInfoDtoDao).getNotesTable();
    }

    @Test
    void testListNotesReturnsEmptySetWhenNoNotes() {
        when(noteFileInfoDtoDao.getNotesTable()).thenReturn(new LinkedHashSet<>(Set.of()));

        Set<NotesTableRowDto> result = service.listNotes();

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "Result should be empty when no notes in DB");

        verify(noteFileInfoDtoDao).getNotesTable();
    }
}
