package app.notesr.cli.util;

import java.nio.file.Path;

public class PathUtils {
    public static Path getTempPath(String pathPart) {
        return Path.of(System.getProperty("java.io.tmpdir"), pathPart);
    }
}
