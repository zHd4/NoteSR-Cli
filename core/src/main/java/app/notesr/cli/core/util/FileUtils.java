package app.notesr.cli.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.text.DecimalFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {
    public static String getNameWithoutExtension(File file) {
        String name = file.getName();
        int dotIndex = name.indexOf('.');
        return (dotIndex == -1) ? name : name.substring(0, dotIndex);
    }

    public static String getReadableSize(long size) {
        String[] units = new String[] {"B", "KB", "MB", "GB", "TB", "PB", "EB"};

        if (size > 0) {
            int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
            return new DecimalFormat("#,##0.#")
                    .format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
        }

        return "0 B";
    }
}
