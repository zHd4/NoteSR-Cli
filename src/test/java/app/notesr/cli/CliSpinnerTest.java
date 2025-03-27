package app.notesr.cli;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CliSpinnerTest {
    private static final int EMULATED_WORK_DURATION = 1000;

    @Test
    public void testSpinner() throws InterruptedException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream mockOut = new PrintStream(outputStream);

        CliSpinner cliSpinner = new CliSpinner("Loading");
        cliSpinner.setPrintStream(mockOut);

        cliSpinner.start();
        emulateWork();
        cliSpinner.stop();

        String output = outputStream.toString();
        assertTrue(output.contains("Loading |")
                && output.contains("Loading /")
                && output.contains("Loading -")
                && output.contains("Loading \\"),
                "Animation output does not contain expected chars");
    }

    private void emulateWork() throws InterruptedException {
        Thread.sleep(EMULATED_WORK_DURATION);
    }
}
