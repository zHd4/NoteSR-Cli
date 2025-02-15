package app.notesr.cli.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    public static boolean isZipArchive(String path) throws IOException {
        File file = new File(path);

        if (!file.exists() || file.isDirectory()) {
            return false;
        }

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] signature = new byte[4];

            if (fileInputStream.read(signature) == 4) {
                return (signature[0] == 0x50 && signature[1] == 0x4B
                        && signature[2] == 0x03 && signature[3] == 0x04);
            }
        }

        return false;
    }

    public static void unzip(String zipPath, String destDir, Thread thread) throws IOException {
        File destDirectory = new File(destDir);

        if (!destDirectory.exists()) {
            destDirectory.mkdirs();
        }

        FileInputStream fileInputStream = new FileInputStream(zipPath);
        ZipInputStream zipInputStream = new ZipInputStream(fileInputStream);

        try (fileInputStream; zipInputStream) {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (thread != null && thread.isInterrupted()) {
                    break;
                }

                File newFile = unzipFile(destDirectory, zipEntry);

                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Cannot create directory: " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();

                    if (parent == null || (!parent.isDirectory() && !parent.mkdirs())) {
                        throw new IOException("Cannot create directory: " + parent);
                    }

                    try (FileOutputStream fileOutputStream = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;

                        while ((len = zipInputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                    }
                }

                zipInputStream.closeEntry();
            }
        }
    }

    private static File unzipFile(File destDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destDir, zipEntry.getName());

        String destDirPath = destDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Invalid record in archive: " + zipEntry.getName());
        }

        return destFile;
    }
}
