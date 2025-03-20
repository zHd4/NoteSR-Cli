package app.notesr.cli.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FixtureUtils {
    public static String readFixture(String path) throws IOException {
        return Files.readString(getFixturePath(path));
    }

    public static Path getFixturePath(String pathPart) {
        return Path.of("src/test/resources/fixtures", pathPart);
    }
}
