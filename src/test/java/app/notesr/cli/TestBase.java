package app.notesr.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TestBase {
    protected static byte[] readFixture(String filename) throws IOException {
        return Files.readAllBytes(getFixturePath(filename));
    }

    protected static Path getFixturePath(String pathPart) {
        return Path.of("src/test/resources/fixtures", pathPart);
    }

    protected static String getTempPath(String pathPart) {
        return Path.of(System.getProperty("java.io.tmpdir"), pathPart).toString();
    }
}
