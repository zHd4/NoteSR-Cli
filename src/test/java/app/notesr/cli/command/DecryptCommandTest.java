package app.notesr.cli.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
}
