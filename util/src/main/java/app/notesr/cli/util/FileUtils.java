package app.notesr.cli.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    public static String getNameWithoutExtension(File file) {
        String name = file.getName();
        int dotIndex = name.indexOf('.');
        return (dotIndex == -1) ? name : name.substring(0, dotIndex);
    }
}
