package app.notesr.cli.service;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.data.dto.NoteOutputDto;
import app.notesr.cli.exception.NoteNotFoundException;
import app.notesr.cli.data.model.Note;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class NoteReadingService {
    private final DbConnection db;

    public NoteOutputDto readNote(String noteId) throws NoteNotFoundException {
        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        FileInfoEntityDao fileInfoEntityDao = db.getConnection().onDemand(FileInfoEntityDao.class);

        Note note = noteEntityDao.getById(noteId);
        Long attachmentsCount = fileInfoEntityDao.getCountByNoteId(noteId);

        if (note == null) {
            throw new NoteNotFoundException(noteId);
        }

        return new NoteOutputDto(note, attachmentsCount);
    }
}
