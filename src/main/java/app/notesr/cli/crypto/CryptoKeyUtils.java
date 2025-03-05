package app.notesr.cli.crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoKeyUtils {
    public static CryptoKey hexToCryptoKey(String hex, String algorithm) {
        String[] hexArray = hex.toLowerCase().split("\\s+");

        byte[] bytes = new byte[hexArray.length];

        byte[] keyBytes = new byte[Aes.KEY_SIZE / 8];
        byte[] salt = new byte[Aes.SALT_SIZE];

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
}
