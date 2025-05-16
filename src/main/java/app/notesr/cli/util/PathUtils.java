package app.notesr.cli.util;

import java.io.File;

public class PathUtils {
    public static String getNameWithoutExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return (dotIndex == -1) ? name : name.substring(0, dotIndex);
    }
}
