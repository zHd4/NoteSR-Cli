package app.notesr.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FixtureUtils {
    public static String readFixture(String path) throws IOException {
        return Files.readString(getFixturePath(path));
    }

    public static Path getFixturePath(String pathPart) {
        String fixturePath = "fixtures/" + pathPart;

        try (InputStream in = Objects.requireNonNull(
                FixtureUtils.class.getClassLoader().getResourceAsStream(fixturePath),
                "Resource not found: " + fixturePath
        )) {

            Path tempFile = Files.createTempFile("fixture-", "-" + Paths.get(fixturePath).getFileName());
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            tempFile.toFile().deleteOnExit();
            return tempFile;

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load test fixture: " + fixturePath, e);
        }
    }
}
