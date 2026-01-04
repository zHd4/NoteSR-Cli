/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.service.file;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.FileInfoDtoDao;
import app.notesr.cli.data.dao.NoteEntityDao;
import app.notesr.cli.data.dto.FilesTableRowDto;
import app.notesr.cli.core.exception.NoteNotFoundException;
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
