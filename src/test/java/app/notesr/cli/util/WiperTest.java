package app.notesr.cli.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WiperTest {
    private static final Random RANDOM = new Random();

    private static final int MIN_FILE_SIZE = 1024;
    private static final int MAX_FILE_SIZE = 1024 * 10;

    @Test
    public void testWipeFile() throws IOException {
        String testFilePath = getTempPath(randomUUID().toString()).toString();
        byte[] testFileContent = new byte[RANDOM.nextInt(MIN_FILE_SIZE, MAX_FILE_SIZE)];

        RANDOM.nextBytes(testFileContent);
        Files.write(Path.of(testFilePath), testFileContent);

        File testFile = new File(testFilePath);
        boolean result = Wiper.wipeFile(testFile);

        assertTrue(result, "Result must be 'true'");
        assertFalse(testFile.exists(), "File must be wiped");
    }

    @Test
    public void testWipeDir() throws IOException {
        String testDirPath = getTempPath(randomUUID().toString()).toString();
        String testFilePath = Path.of(testDirPath, randomUUID().toString()).toString();

        byte[] testFileContent = new byte[RANDOM.nextInt(MIN_FILE_SIZE, MAX_FILE_SIZE)];
        RANDOM.nextBytes(testFileContent);

        File testDir = new File(testDirPath);
        File testFile = new File(testFilePath);

        boolean isDirCreated = testDir.mkdir();
        assertTrue(isDirCreated, "Cannot create test directory " + testDirPath);

        Files.write(testFile.toPath(), testFileContent);

        boolean result = Wiper.wipeDir(testDir);
        assertTrue(result, "Result must be 'true'");

        assertFalse(testDir.exists(), "Dir must be wiped");
        assertFalse(testFile.exists(), "File must be wiped");
    }
}
