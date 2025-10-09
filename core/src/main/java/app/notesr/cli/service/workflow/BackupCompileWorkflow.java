package app.notesr.cli.service.workflow;

import app.notesr.cli.crypto.FileEncryptionException;

import app.notesr.cli.dto.CryptoSecrets;
import app.notesr.cli.exception.BackupEncryptionException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.service.BackupCompilationService;
import app.notesr.cli.service.BackupEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
@RequiredArgsConstructor
public final class BackupCompileWorkflow {
    private final BackupCompilationService compilationService;
    private final BackupEncryptionService encryptionService;

    public void run(File dbFile, File tempArchive, File outputFile, CryptoSecrets secrets, String noteSrVersion)
            throws BackupIOException, BackupEncryptionException, FileEncryptionException {

        compilationService.compile(dbFile, tempArchive, secrets, noteSrVersion);
        encryptionService.encrypt(tempArchive, outputFile, secrets);
    }
}
