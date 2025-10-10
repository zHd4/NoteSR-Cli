package app.notesr.cli.service.workflow;

import app.notesr.cli.exception.BackupDecryptionException;
import app.notesr.cli.dto.CryptoSecrets;
import app.notesr.cli.service.BackupDecryptionService;
import app.notesr.cli.service.BackupParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class BackupDecryptWorkflow {
    private final BackupDecryptionService backupDecryptionService;
    private final BackupParsingService parsingService;

    public void run(File encryptedBackup, CryptoSecrets secrets, File outputFile, List<File> tempFiles)
            throws IOException, BackupDecryptionException {
        log.info("Decrypting {}", encryptedBackup.getAbsolutePath());
        File decrypted = backupDecryptionService.decrypt(encryptedBackup, secrets);
        tempFiles.add(decrypted);
        log.info("Decryption finished successfully");

        log.info("Generating database");
        Path tempDir = parsingService.parse(decrypted, outputFile, secrets);
        tempFiles.add(tempDir.toFile());
        log.info("Successfully generated");
    }
}
