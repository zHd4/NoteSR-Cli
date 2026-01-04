/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.service.note;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.NoteFileInfoDtoDao;
import app.notesr.cli.data.dto.NotesTableRowDto;
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
