package app.notesr.cli.core.util.test;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FixtureUtils {
    public static String readFixture(String pathPart, Path tempDirPath) throws IOException {
        return Files.readString(getFixturePath(pathPart, tempDirPath));
    }

    public static Path getFixturePath(String pathPart, Path tempDirPath) {
        ClassLoader classLoader = FixtureUtils.class.getClassLoader();
        String fixturePath = "fixtures/" + pathPart;

        try (InputStream in = Objects.requireNonNull(classLoader.getResourceAsStream(fixturePath),
                "Resource not found: " + fixturePath)) {
            Path tempFilePath = tempDirPath.resolve(Path.of(fixturePath).getFileName());
            Files.copy(in, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
            tempFilePath.toFile().deleteOnExit();
            return tempFilePath;

        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load test fixture: " + fixturePath, e);
        }
    }
}
