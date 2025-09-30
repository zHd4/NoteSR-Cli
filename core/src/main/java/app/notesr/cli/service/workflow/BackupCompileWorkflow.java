package app.notesr.cli.service.workflow;

import app.notesr.cli.crypto.FileEncryptionException;

import app.notesr.cli.dto.CryptoSecrets;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.service.BackupCompilationService;
import app.notesr.cli.service.BackupEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@RequiredArgsConstructor
public final class BackupCompileWorkflow {
    private final BackupCompilationService compilationService;
    private final BackupEncryptionService encryptionService;

    public Path run(File dbFile, File tempArchive, File outputFile, CryptoSecrets secrets, String noteSrVersion)
            throws BackupIOException, BackupDbException, IOException, FileEncryptionException {

        Path tempDir = compilationService.compile(dbFile, tempArchive, noteSrVersion);
        encryptionService.encrypt(tempArchive, outputFile, secrets);
        return tempDir;
    }
}
