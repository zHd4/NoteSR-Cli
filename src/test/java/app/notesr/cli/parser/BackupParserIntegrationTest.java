package app.notesr.cli.parser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;

public class BackupParserIntegrationTest {
    private static final String BACKUP_V1_FIXTURE_NAME = "backup-v1.json";
    private static final String BACKUP_V2_FIXTURE_NAME = "backup-v2.zip";

    private Path parserTempDirPath;
    private Path dbPath;

    @BeforeEach
    public void beforeEach() {
        String uuid = randomUUID().toString();

        parserTempDirPath = Path.of(getTempPath(uuid) + "_temp");
        dbPath = Path.of(getTempPath(uuid) + ".db");
    }

    @ParameterizedTest
    @ValueSource(strings = {BACKUP_V1_FIXTURE_NAME, BACKUP_V2_FIXTURE_NAME})
    public void testParser(String backupFixtureName) {
        Path backupPath = getFixturePath("parser/backup_parser/" + backupFixtureName);
        BackupParser parser = new BackupParser(backupPath, dbPath);

        parser.setTempDirPath(parserTempDirPath);
        parser.run();
    }

    @AfterEach
    public void afterEach() throws IOException {
        deleteDir(parserTempDirPath);
        Files.delete(dbPath);
    }

    public static void deleteDir(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
