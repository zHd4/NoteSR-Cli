/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.command;

import app.notesr.cli.core.exception.BackupDecryptionException;
import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.core.exception.BackupDbException;
import app.notesr.cli.core.exception.BackupIOException;
import app.notesr.cli.service.parser.BackupParserException;
import app.notesr.cli.service.parser.UnexpectedFieldException;
import app.notesr.cli.service.parser.BackupParsingService;
import app.notesr.cli.service.parser.BackupDecryptWorkflow;
import app.notesr.cli.service.parser.BackupDecryptionService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@CommandLine.Command(name = "decrypt",
        description = "Decrypts exported NoteSR .notesr.bak file and converts it to a SQLite database.")
public final class DecryptCommand extends Command {

    @CommandLine.Parameters(index = "0", paramLabel = "file_path",
            description = "path to encrypted NoteSR .notesr.bak file")
    private String encryptedBackupPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output SQLite database path")
    private String outputFilePath;

    public DecryptCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode;

        File outputFile = null;
        List<File> tempFiles = new ArrayList<>();

        try {
            File encryptedFile = getFile(encryptedBackupPath);
            File keyFile = getFile(keyPath);
            outputFile = getOutputFile(encryptedFile,
                    outputFilePath != null ? Path.of(outputFilePath) : null, ".notesr.db");

            decrypt(encryptedFile, keyFile, outputFile, tempFiles);
            exitCode = SUCCESS;

            log.info("Saved to: {}", outputFile.getAbsolutePath());
        } catch (CommandHandlingException e) {
            if (outputFile != null && outputFile.exists()) {
                tempFiles.add(outputFile);
            }

            exitCode = e.getExitCode();
        } finally {
            cleanTempFiles(tempFiles);
        }

        return exitCode;
    }

    private void decrypt(File encryptedBackupFile, File keyFile, File outputDbFile, List<File> tempFiles)
            throws CommandHandlingException {
        try {
            CryptoSecrets secrets = getCryptoSecrets(keyFile);

            BackupDecryptionService backupDecryptionService = new BackupDecryptionService();
            BackupParsingService backupParsingService = new BackupParsingService();

            BackupDecryptWorkflow workflow = new BackupDecryptWorkflow(backupDecryptionService, backupParsingService);
            workflow.run(encryptedBackupFile, secrets, outputDbFile, tempFiles);
        } catch (BackupDecryptionException e) {
            log.error("{}: failed to decrypt, invalid key or file corrupted", encryptedBackupPath);
            log.debug("E: ", e);
            throw new CommandHandlingException(CRYPTO_ERROR);
        } catch (BackupIOException | BackupParserException | UnexpectedFieldException e) {
            log.error("{}: failed to parse, details:\n{}", encryptedBackupPath, e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (BackupDbException e) {
            log.error("Failed to write data to database, details:\n{}", e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(DB_ERROR);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }
}
