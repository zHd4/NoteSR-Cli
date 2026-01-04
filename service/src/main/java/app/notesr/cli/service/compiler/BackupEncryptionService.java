/*
 * Copyright (c) 2026 zHd4
 * SPDX-License-Identifier: MIT
 */
 
package app.notesr.cli.service.compiler;

import app.notesr.cli.core.security.crypto.AesCryptor;
import app.notesr.cli.core.security.crypto.AesGcmCryptor;
import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.core.exception.BackupEncryptionException;
import app.notesr.cli.core.exception.BackupIOException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static app.notesr.cli.core.util.KeyUtils.getSecretKeyFromSecrets;

@Slf4j
public final class BackupEncryptionService {
    public void encrypt(File inputFile, File outputFile, CryptoSecrets secrets) throws BackupEncryptionException {

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            log.info("Encrypting {}", inputFile.getAbsolutePath());

            AesCryptor cryptor = new AesGcmCryptor(getSecretKeyFromSecrets(secrets));
            cryptor.encrypt(inputStream, outputStream);

            log.info("Encryption finished successfully");
        } catch (GeneralSecurityException e) {
            throw new BackupEncryptionException(e);
        } catch (IOException e) {
            throw new BackupIOException(e);
        }
    }
}
