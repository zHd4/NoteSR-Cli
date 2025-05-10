package app.notesr.cli.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

import static app.notesr.cli.util.FixtureUtils.getFixturePath;
import static app.notesr.cli.util.PathUtils.getTempPath;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipUtilsTest {
    private static final Random RANDOM = new Random();

    private static final String DIR_PATH = getFixturePath("exported").toString();
    private static final String ZIP_PATH = getFixturePath("exported.zip").toString();

    private static final String TEMP_EXTRACTED_DIR_PATH = getTempPath(randomUUID().toString()).toString();
    private static final String TEMP_ZIP_PATH = getTempPath(randomUUID() + ".zip").toString();

    @Test
    void testZipDirectory() throws IOException {
        ZipUtils.zipDirectory(DIR_PATH, TEMP_ZIP_PATH);
        File zipFile = new File(TEMP_ZIP_PATH);

        assertTrue(zipFile.exists(), "Zip file not found");
    }

    @Test
    void testUnzip() throws IOException {
        ZipUtils.unzip(ZIP_PATH, TEMP_EXTRACTED_DIR_PATH);
        File dir = new File(TEMP_EXTRACTED_DIR_PATH);

        assertTrue(dir.exists(), "Directory " + dir.getAbsolutePath() + " not found");
        assertTrue(isDirsIdentical(DIR_PATH, TEMP_EXTRACTED_DIR_PATH), "Dirs not identical");
    }

    @Test
    void testIsZipArchive() throws IOException {
        File nonZipFile = getTempPath(randomUUID().toString()).toFile();
        byte[] nonZipFileData = new byte[1024];

        RANDOM.nextBytes(nonZipFileData);
        Files.write(Path.of(nonZipFile.getAbsolutePath()), nonZipFileData);

        assertFalse(ZipUtils.isZipArchive(nonZipFile.getAbsolutePath()));
        assertFalse(ZipUtils.isZipArchive(DIR_PATH));
        assertTrue(ZipUtils.isZipArchive(ZIP_PATH));
        assertTrue(nonZipFile.delete());
    }

    @AfterAll
    public static void afterAll() throws IOException {
        File tempDir = new File(TEMP_EXTRACTED_DIR_PATH);
        File tempArchive = new File(TEMP_ZIP_PATH);

        if (tempDir.exists()) {
            removeDir(tempDir);
        }

        if (tempArchive.exists()) {
            Files.delete(tempArchive.toPath());
        }
    }

    private static boolean isDirsIdentical(String path1, String path2) {
        File dir1 = new File(path1);
        File dir2 = new File(path2);

        if (!dir1.isDirectory() || !dir2.isDirectory()) {
            throw new IllegalArgumentException("Both inputs must be directories");
        }

        File[] dir1Files = dir1.listFiles();
        File[] dir2Files = dir2.listFiles();

        if (dir1Files == null || dir2Files == null || dir1Files.length != dir2Files.length) {
            return false;
        }

        for (File file1 : dir1Files) {
            File file2 = new File(dir2, file1.getName());

            if (file1.isDirectory()) {
                if (!file2.exists() || !file2.isDirectory()
                        || !isDirsIdentical(file1.getPath(), file2.getPath())) {
                    return false;
                }
            } else {
                if (!file2.exists() || !file2.isFile()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static void removeDir(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();

            if (children != null) {
                for (File child : children) {
                    removeDir(child);
                }
            }
        }

        if (!dir.delete()) {
            throw new RuntimeException("Cannot delete directory " + dir.getAbsolutePath());
        }
    }
}
