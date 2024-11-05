package app.notesr.cli.crypto;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class BackupDecryptorTest {
    private static FileInputStream getFixtureFileInputStream(String filename) throws IOException {
        return (FileInputStream) Files.newInputStream(generateFixturePath(filename));
    }

    private static byte[] readFixture(String filename) throws IOException {
        return Files.readAllBytes(generateFixturePath(filename));
    }

    private static Path generateFixturePath(String filename) {
        return Path.of("src/test/resources/fixtures/" + filename);
    }
}