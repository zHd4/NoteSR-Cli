package app.notesr.cli.service;

import app.notesr.cli.parser.BackupParserException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static app.notesr.cli.core.util.test.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupParsingServiceTest {

    private final BackupParsingService service = new BackupParsingService();

    @TempDir
    private Path tempDir;

    @Test
    void parseWithValidDecryptedFileReturnsTempDirPath() {
        File decryptedBackupFile = getFixturePath("v2.notesr.bak.decrypted", tempDir).toFile();
        File outputDbFile = tempDir.resolve("output.db").toFile();

        Path parserTempDir = service.parse(decryptedBackupFile, outputDbFile, null);

        assertNotNull(parserTempDir, "Temp directory path should not be null");
        assertTrue(parserTempDir.toFile().exists(), "Temp directory should exist");
    }

    @Test
    void parseWithValidDecryptedFileDoesNotReturnsExistingTempDirPath() {
        File decryptedBackupFile = getFixturePath("v1.notesr.bak.decrypted", tempDir).toFile();
        File outputDbFile = tempDir.resolve("output.db").toFile();

        Path parserTempDir = service.parse(decryptedBackupFile, outputDbFile, null);

        assertNotNull(parserTempDir, "Temp directory path should not be null");
        assertFalse(parserTempDir.toFile().exists(), "Temp directory should not exist");
    }

    @Test
    void parseWithCorruptedFileThrowsBackupIOException() throws IOException {
        File corruptedBackupFile = tempDir.resolve("corrupted.bak.decrypted").toFile();
        File outputDbFile = tempDir.resolve("output.db").toFile();

        Files.writeString(corruptedBackupFile.toPath(), "corrupted_file");

        assertThrows(BackupParserException.class, () ->
                service.parse(corruptedBackupFile, outputDbFile, null),
                "Corrupted backup file should throw BackupIOException");
    }
}
