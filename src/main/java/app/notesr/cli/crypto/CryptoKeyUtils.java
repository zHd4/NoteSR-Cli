package app.notesr.cli.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class CryptoKeyUtils {
    private static final int KEY_SIZE = 256;
    private static final int SALT_SIZE = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static CryptoKey hexToCryptoKey(String hex, String algorithm) {
        String[] hexArray = hex.toLowerCase().split("\\s+");

        byte[] bytes = new byte[hexArray.length];

        byte[] keyBytes = new byte[KEY_SIZE / 8];
        byte[] salt = new byte[SALT_SIZE];

        for (int i = 0; i < hexArray.length; i++) {
            String hexDigit = hexArray[i];
            if (!hexDigit.isBlank()) {
                bytes[i] = (byte) Integer.parseInt(hexDigit, 16);
            }
        }

        System.arraycopy(bytes, 0, keyBytes, 0, keyBytes.length);
        System.arraycopy(bytes, keyBytes.length, salt, 0, salt.length);

        SecretKey key = new SecretKeySpec(keyBytes, 0, keyBytes.length, algorithm);
        return new CryptoKey(key, salt);
    }

    public static String getRandomCryptoKeyHex() {
        final int rows = 12;
        final int columns = 4;

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                int value = SECURE_RANDOM.nextInt(256);
                builder.append(String.format("%02X", value));
                if (j < columns - 1) {
                    builder.append(" ");
                }
            }
            if (i < rows - 1) {
                builder.append(System.lineSeparator());
            }
        }

        return builder.toString();
    }
}
