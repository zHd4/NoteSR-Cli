package app.notesr.cli.service;

import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.dto.CryptoKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public final class DecryptWorkflow {
    private final BackupDecryptionService backupDecryptionService;
    private final BackupParsingService parsingService;

    public void run(File encryptedBackup, CryptoKey cryptoKey, File outputFile, List<File> tempFiles)
            throws IOException, FileDecryptionException {
        log.info("Decrypting {}", encryptedBackup.getAbsolutePath());
        File decrypted = backupDecryptionService.decrypt(encryptedBackup, cryptoKey);
        tempFiles.add(decrypted);
        log.info("Decryption finished successfully");

        log.info("Generating database");
        Path tempDir = parsingService.parse(decrypted, outputFile);
        tempFiles.add(tempDir.toFile());
        log.info("Successfully generated");
    }
}
