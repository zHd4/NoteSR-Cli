/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */

package app.notesr.cli.service.parser;

import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.core.exception.BackupDbException;
import app.notesr.cli.core.exception.BackupIOException;

import java.io.File;
import java.nio.file.Path;

public final class BackupParsingService {
    public Path parse(File decryptedBackup, File outputFile, CryptoSecrets secrets)
            throws BackupIOException, BackupParserException, UnexpectedFieldException, BackupDbException {

        BackupParser parser = new BackupParser(decryptedBackup.toPath(), outputFile.toPath(), secrets);
        parser.run();

        return parser.getTempDirPath();
    }
}
