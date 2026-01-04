/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.core.util;

import static java.util.Objects.requireNonNull;

import app.notesr.cli.core.security.dto.CryptoSecrets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KeyUtils {
    private static final int KEY_SIZE = 256;
    private static final String KEY_GENERATOR_ALGORITHM = "AES";

    public static SecretKey getSecretKeyFromSecrets(CryptoSecrets cryptoSecrets) {
        int keyLength = KEY_SIZE / 8;
        byte[] keyBytes = Arrays.copyOfRange(cryptoSecrets.getKey(), 0, keyLength);

        return new SecretKeySpec(keyBytes, KEY_GENERATOR_ALGORITHM);
    }

    public static CryptoSecrets getSecretsFromHex(String hex) {
        return new CryptoSecrets(getKeyBytesFromHex(hex));
    }

    public static byte[] getKeyBytesFromHex(String hex) {
        try {
            requireNonNull(hex, "hex must not be null");

            String[] hexArray = hex.toLowerCase().trim().split("\\s+");
            byte[] key = new byte[hexArray.length];

            for (int i = 0; i < hexArray.length; i++) {
                key[i] = (byte) Integer.parseInt(hexArray[i], 16);
            }

            return key;
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid hex key", e);
        }
    }

    public static byte[] getIvFromSecrets(CryptoSecrets cryptoSecrets) {
        int ivSize = cryptoSecrets.getKey().length - (KEY_SIZE / 8);
        byte[] iv = new byte[ivSize];

        System.arraycopy(cryptoSecrets.getKey(), KEY_SIZE / 8, iv, 0, ivSize);
        return iv;
    }
}
