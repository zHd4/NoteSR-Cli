/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.service.file;

import app.notesr.cli.data.DbConnection;
import app.notesr.cli.data.dao.DataBlockEntityDao;
import app.notesr.cli.data.dao.FileInfoEntityDao;
import app.notesr.cli.data.model.DataBlock;
import app.notesr.cli.data.model.FileInfo;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashSet;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FileExtractionServiceTest {
    private FileInfoEntityDao fileInfoEntityDao;
    private DataBlockEntityDao dataBlockEntityDao;
    private FileExtractionService service;

    @TempDir
    private File tempDir;

    private String fileId;

    @BeforeEach
    void setUp() {
        Jdbi jdbi = mock(Jdbi.class);
        DbConnection db = mock(DbConnection.class);
        when(db.getConnection()).thenReturn(jdbi);

        fileInfoEntityDao = mock(FileInfoEntityDao.class);
        when(jdbi.onDemand(FileInfoEntityDao.class)).thenReturn(fileInfoEntityDao);

        dataBlockEntityDao = mock(DataBlockEntityDao.class);
        when(jdbi.onDemand(DataBlockEntityDao.class)).thenReturn(dataBlockEntityDao);

        service = new FileExtractionService(db);
        fileId = randomUUID().toString();
    }

    @Test
    void getFileInfoReturnsCorrectFileInfo() {

        FileInfo mockFileInfo = FileInfo.builder()
                .id(fileId)
                .noteId("note-123")
                .name("image.png")
                .type("image/png")
                .size(1024L)
                .build();

        when(fileInfoEntityDao.getById(fileId)).thenReturn(mockFileInfo);

        FileInfo actual = service.getFileInfo(fileId);

        assertNotNull(actual, "Actual must be not null");
        assertEquals(mockFileInfo.getName(), actual.getName(), "Unexpected name");
        assertEquals(mockFileInfo.getNoteId(), actual.getNoteId(), "Unexpected note id");
        assertEquals(mockFileInfo.getType(), actual.getType(), "Unexpected type");
        assertEquals(mockFileInfo.getSize(), actual.getSize(), "Unexpected size");
    }

    @Test
    void extractFileWritesAllDataBlocks() throws IOException {
        File outputFile = new File(tempDir, "output.txt");

        LinkedHashSet<String> blockIds = new LinkedHashSet<>();
        blockIds.add("b1");
        blockIds.add("b2");

        when(dataBlockEntityDao.getIdsByFileId(fileId)).thenReturn(blockIds);
        when(dataBlockEntityDao.getById("b1")).thenReturn(DataBlock.builder().data("Hello ".getBytes()).build());
        when(dataBlockEntityDao.getById("b2")).thenReturn(DataBlock.builder().data("World!".getBytes()).build());

        service.extractFile(fileId, outputFile);

        String actual = Files.readString(outputFile.toPath());
        assertEquals("Hello World!", actual, "Unexpected file content");
    }

    @Test
    void extractFileThrowsIfDataBlockIsNull() {
        File outputFile = new File(tempDir, "corrupted.txt");

        LinkedHashSet<String> blockIds = new LinkedHashSet<>();
        blockIds.add("bad-block");

        when(dataBlockEntityDao.getIdsByFileId(fileId)).thenReturn(blockIds);
        when(dataBlockEntityDao.getById("bad-block")).thenReturn(null);

        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> service.extractFile(fileId, outputFile), "Exception must be thrown");

        assertTrue(ex.getMessage().contains("bad-block"), "Exception message must contain block id");
    }

    @Test
    void extractFileThrowsIfDataIsNull() {
        File outputFile = new File(tempDir, "corrupted2.txt");

        LinkedHashSet<String> blockIds = new LinkedHashSet<>();
        blockIds.add("b3");

        when(dataBlockEntityDao.getIdsByFileId(fileId)).thenReturn(blockIds);
        when(dataBlockEntityDao.getById("b3")).thenReturn(DataBlock.builder().data(null).build());

        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> service.extractFile(fileId, outputFile), "Exception must be thrown");

        assertTrue(ex.getMessage().contains("b3"), "Exception message must contain block id");
    }

    @Test
    void extractFileCreatesEmptyFileIfNoBlocks() throws IOException {
        File outputFile = new File(tempDir, "empty.txt");

        LinkedHashSet<String> blockIds = new LinkedHashSet<>();
        when(dataBlockEntityDao.getIdsByFileId(fileId)).thenReturn(blockIds);

        service.extractFile(fileId, outputFile);

        assertTrue(outputFile.exists(), "Expected file not found");
        assertEquals(0, outputFile.length(), "File must be empty");
    }
}
