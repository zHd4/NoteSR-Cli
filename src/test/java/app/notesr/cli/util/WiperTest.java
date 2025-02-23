package app.notesr.cli.util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WiperTest {
    private static final Random RANDOM = new Random();

    private static final int MIN_FILE_SIZE = 1024;
    private static final int MAX_FILE_SIZE = 1024 * 10;

    @Test
    public void testWipeFile() throws IOException {
        String testFilePath = PathUtils.getTempPath(randomUUID().toString());
        byte[] testFileContent = new byte[RANDOM.nextInt(MAX_FILE_SIZE, MAX_FILE_SIZE)];

        RANDOM.nextBytes(testFileContent);
        Files.write(Path.of(testFilePath), testFileContent);

        File testFile = new File(testFilePath);
        boolean result = Wiper.wipeFile(testFile);

        assertTrue(result, "Result must be 'true'");
        assertFalse(testFile.exists(), "File must be wiped");
    }
}
