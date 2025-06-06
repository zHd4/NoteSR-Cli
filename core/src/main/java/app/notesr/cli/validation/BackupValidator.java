package app.notesr.cli.validation;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import static app.notesr.cli.util.ZipUtils.getTopLevelEntries;
import static app.notesr.cli.util.ZipUtils.isZipArchive;

public class BackupValidator {
    private static final byte[] V1_SIGNATURE = "{\"version\":".getBytes();

    private static final Set<String> V2_ZIP_ENTRIES = Set.of(
            "data_blocks/", "files_info.json", "notes.json", "version");

    public static boolean isValid(String backupPath) throws IOException {
        return isV1Format(backupPath) || isV2Format(backupPath);
    }

    private static boolean isV1Format(String backupPath) throws IOException {
        try (FileInputStream fileInputStream = new FileInputStream(backupPath)) {
            byte[] signature = new byte[V1_SIGNATURE.length];

            if (fileInputStream.read(signature) == V1_SIGNATURE.length) {
                return Arrays.equals(signature, V1_SIGNATURE);
            }
        }

        return false;
    }

    private static boolean isV2Format(String backupPath) throws IOException {
        return isZipArchive(backupPath) && V2_ZIP_ENTRIES.equals(getTopLevelEntries(backupPath));
    }
}
