package app.notesr.cli.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static app.notesr.cli.command.Command.FILE_RW_ERROR;
import static app.notesr.cli.command.Command.SUCCESS;
import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.PathUtils.getNameWithoutExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompileCommandTest {
    private static final Random RANDOM = new Random();

    private CommandLine cmd;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        CompileCommand compileCommand = new CompileCommand();
        cmd = new CommandLine(compileCommand);
    }

    @Test
    void testWithAllArgs() {
        final String noteSrVersion = "5.1";

        Path dbPath = getFixturePath("backup.db");
        Path keyPath = getFixturePath("crypto_key.txt");

        int exitCode = cmd.execute(dbPath.toString(), keyPath.toString(), "-o", tempDir.toString(),
                "-n", noteSrVersion);
        assertEquals(SUCCESS, exitCode, "Expected code " + SUCCESS);

        Path outputPath = tempDir.resolve(getNameWithoutExtension(dbPath.toFile()) + ".notesr.bak");
        assertTrue(outputPath.toFile().exists(), "Output file " + outputPath + " not found");
    }

    @ParameterizedTest
    @ValueSource(strings = {"C:\\folder\\..\\NUL\\file", "/////some///weird//path///file"})
    void testWithInvalidFilesPaths(String path) {
        String dbPath = path + ".db";
        String keyPath = path + ".txt";

        int exitCode = cmd.execute(dbPath, keyPath, "-o", tempDir.toString());
        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testWithInvalidKeyAsString() throws IOException {
        String invalidKey = "TEST_INVALID_KEY";

        Path invalidKeyPath = tempDir.resolve("invalid_key.txt");
        Path dbPath = getFixturePath("backup.db");

        Files.writeString(invalidKeyPath, invalidKey);
        int exitCode = cmd.execute(dbPath.toString(), invalidKeyPath.toString(), "-o", tempDir.toString());

        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }

    @Test
    void testWithInvalidKeyAsBinary() throws IOException {
        byte[] invalidKey = new byte[1024];
        RANDOM.nextBytes(invalidKey);

        Path invalidKeyPath = tempDir.resolve("invalid_key.txt");
        Path dbPath = getFixturePath("backup.db");

        Files.write(invalidKeyPath, invalidKey);
        int exitCode = cmd.execute(dbPath.toString(), invalidKeyPath.toString(), "-o", tempDir.toString());

        assertEquals(FILE_RW_ERROR, exitCode, "Expected code " + FILE_RW_ERROR);
    }
}