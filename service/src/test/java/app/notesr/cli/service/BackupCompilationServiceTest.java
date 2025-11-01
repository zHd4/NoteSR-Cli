package app.notesr.cli.service;


import app.notesr.cli.security.crypto.dto.CryptoSecrets;
import app.notesr.cli.util.ZipUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.security.SecureRandom;

import static app.notesr.cli.util.test.FixtureUtils.getFixturePath;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupCompilationServiceTest {

    private static final int KEY_SIZE = 48;

    @TempDir
    private Path tempDir;

    @Test
    void testCompile() throws Exception {
        File dbFile = getFixturePath("shared/backup.db", tempDir).toFile();
        File outputFile = tempDir.resolve("output.zip").toFile();

        byte[] keyBytes = new byte[KEY_SIZE];
        SecureRandom.getInstanceStrong().nextBytes(keyBytes);

        CryptoSecrets secrets = new CryptoSecrets(keyBytes);
        BackupCompilationService service = new BackupCompilationService();

        service.compile(dbFile, outputFile, secrets, "5.2.3");

        assertTrue(outputFile.exists(), "The output file was not created");
        assertTrue(outputFile.isFile(), "The output file must be a regular file");
        assertTrue(ZipUtils.isZipArchive(outputFile.getAbsolutePath()),
                "The output file is not a valid zip archive");
    }
}
