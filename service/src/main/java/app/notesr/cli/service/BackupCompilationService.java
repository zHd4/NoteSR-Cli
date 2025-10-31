package app.notesr.cli.service;

import app.notesr.cli.compiler.BackupCompiler;
import app.notesr.cli.crypto.dto.CryptoSecrets;
import app.notesr.cli.exception.BackupEncryptionException;
import app.notesr.cli.exception.BackupIOException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public final class BackupCompilationService {
    public void compile(File dbFile, File outputFile, CryptoSecrets secrets, String noteSrVersion)
            throws BackupIOException, BackupEncryptionException {

        log.info("Compiling {}", dbFile.getAbsolutePath());
        BackupCompiler compiler = new BackupCompiler(dbFile.toPath(), outputFile.toPath(), secrets, noteSrVersion);
        compiler.run();

        log.info("Compiling finished successfully");
    }
}
