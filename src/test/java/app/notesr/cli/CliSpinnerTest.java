package app.notesr.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Deprecated(forRemoval = true)
@ExtendWith(MockitoExtension.class)
public final class CliSpinnerTest {
    private static final int EMULATED_WORK_DURATION = 1000;

    @Mock
    private Logger mockLogger;

    private CliSpinner cliSpinner;
    private ByteArrayOutputStream outputStream;

    @BeforeEach
    void beforeEach() {
        cliSpinner = new CliSpinner("Loading", mockLogger);
        outputStream = new ByteArrayOutputStream();

        PrintStream mockOut = new PrintStream(outputStream);
        cliSpinner.setPrintStream(mockOut);
    }

    @Test
    public void testSpinner() throws Exception {
        when(mockLogger.isInfoEnabled()).thenReturn(false);
        when(mockLogger.isDebugEnabled()).thenReturn(false);
        when(mockLogger.isTraceEnabled()).thenReturn(false);

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

    @Test
    public void testSpinnerWhenNotAvailable() {
        when(mockLogger.isInfoEnabled()).thenReturn(true);

        assertFalse(cliSpinner.isAvailable(), "The spinner must be unavailable");
        assertThrows(UnsupportedOperationException.class, cliSpinner::start,
                "Method 'start' must throw exception");
        assertEquals(0, outputStream.size(), "Output must be empty");
    }

    private void emulateWork() throws InterruptedException {
        Thread.sleep(EMULATED_WORK_DURATION);
    }
}
