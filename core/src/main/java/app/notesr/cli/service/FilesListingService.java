package app.notesr.cli.service;

import app.notesr.cli.db.DbConnection;
import app.notesr.cli.db.dao.FileInfoDtoDao;
import app.notesr.cli.db.dao.NoteEntityDao;
import app.notesr.cli.dto.FilesTableRowDto;
import app.notesr.cli.exception.NoteNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public final class FilesListingService {
    private final DbConnection db;

    public Set<FilesTableRowDto> listFiles(String noteId) throws NoteNotFoundException {
        NoteEntityDao noteEntityDao = db.getConnection().onDemand(NoteEntityDao.class);
        FileInfoDtoDao fileInfoDtoDao = db.getConnection().onDemand(FileInfoDtoDao.class);

        if (noteEntityDao.getById(noteId) == null) {
            throw new NoteNotFoundException(noteId);
        }

        return fileInfoDtoDao.getFilesTableRowsByNoteId(noteId);
    }
}
