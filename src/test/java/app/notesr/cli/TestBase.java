package app.notesr.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestBase {
    protected static byte[] readFixture(String filename) throws IOException {
        return Files.readAllBytes(getFixturePath(filename));
    }

    protected static Path getFixturePath(String filename) {
        return Path.of("src/test/resources/fixtures", filename);
    }
}
