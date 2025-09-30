package app.notesr.cli.validation;

import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Set;

import static app.notesr.cli.util.ZipUtils.getTopLevelEntries;
import static app.notesr.cli.util.ZipUtils.isZipArchive;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class BackupValidator {
    private static final byte[] V1_SIGNATURE = "{\"version\":".getBytes();

    private static final Set<String> V2_ZIP_ENTRIES = Set.of(
            "data_blocks/", "files_info.json", "notes.json", "version");

    private static final Set<String> V3_ZIP_ENTRIES = Set.of(
            "notes/", "finfo/", "binfo/", "fblobs/", "version");

    public static boolean isValid(String backupPath) throws UncheckedIOException {
        return isV1Format(backupPath) || isV2Format(backupPath) || isV3Format(backupPath);
    }

    public static boolean isV1Format(String backupPath) throws UncheckedIOException {
        try (FileInputStream fileInputStream = new FileInputStream(backupPath)) {
            byte[] signature = new byte[V1_SIGNATURE.length];

            if (fileInputStream.read(signature) == V1_SIGNATURE.length) {
                return Arrays.equals(signature, V1_SIGNATURE);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return false;
    }

    public static boolean isV2Format(String backupPath) throws UncheckedIOException {
        try {
            return isZipArchive(backupPath) && V2_ZIP_ENTRIES.equals(getTopLevelEntries(backupPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean isV3Format(String backupPath) throws UncheckedIOException {
        try {
            return isZipArchive(backupPath) && V3_ZIP_ENTRIES.equals(getTopLevelEntries(backupPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
