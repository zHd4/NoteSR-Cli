package app.notesr.cli.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import picocli.CommandLine;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DecryptCommandTest {
    private CommandLine cmd;

    @BeforeEach
    public void beforeEach() {
        DecryptCommand decryptCommand = new DecryptCommand();
        cmd = new CommandLine(decryptCommand);
    }

    @Test
    public void testWithoutArgs() {
        int exitCode = cmd.execute();
        assertEquals(DecryptCommand.FILE_RW_ERROR, exitCode, "Expected code " + DecryptCommand.FILE_RW_ERROR);
    }

    @ParameterizedTest
    @ValueSource(strings = {"C:\\folder\\..\\NUL\\file", "/////some///weird//path///file"})
    public void testWithInvalidFilesPaths(String path) {
        String filePath = path + ".notesr.bak";
        String keyPath = path + ".txt";

        int exitCode = cmd.execute(filePath, keyPath);
        assertEquals(DecryptCommand.FILE_RW_ERROR, exitCode, "Expected code " + DecryptCommand.FILE_RW_ERROR);
    }
}
