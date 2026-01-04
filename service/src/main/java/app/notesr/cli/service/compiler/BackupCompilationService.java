/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.service.compiler;

import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.core.exception.BackupEncryptionException;
import app.notesr.cli.core.exception.BackupIOException;
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
