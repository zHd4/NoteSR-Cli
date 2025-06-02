package app.notesr.cli.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static String computeSha512(String filePath) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");

        try (FileInputStream stream = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = stream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        StringBuilder hexDigestBuilder = new StringBuilder();

        for (byte b : digest.digest()) {
            hexDigestBuilder.append(String.format("%02x", b));
        }

        return hexDigestBuilder.toString();
    }
}
