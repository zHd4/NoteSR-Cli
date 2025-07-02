package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.NoteFileInfoDtoDao;
import app.notesr.cli.dto.NotesTableRowDto;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public final class NotesListingService {
    private final DbConnection db;

    public Set<NotesTableRowDto> listNotes() {
        NoteFileInfoDtoDao noteFileInfoDtoDao = db.getConnection().onDemand(NoteFileInfoDtoDao.class);
        return noteFileInfoDtoDao.getNotesTable();
    }
}
