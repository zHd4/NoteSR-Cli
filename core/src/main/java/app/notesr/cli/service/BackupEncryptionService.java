package app.notesr.cli.service;

import app.notesr.cli.crypto.AesCryptor;
import app.notesr.cli.crypto.AesGcmCryptor;
import app.notesr.cli.crypto.FileEncryptionException;
import app.notesr.cli.dto.CryptoSecrets;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;

import static app.notesr.cli.util.KeyUtils.getSecretKeyFromSecrets;

@Slf4j
public final class BackupEncryptionService {
    public void encrypt(File inputFile, File outputFile, CryptoSecrets secrets)
            throws IOException, FileEncryptionException {

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {

            log.info("Encrypting {}", inputFile.getAbsolutePath());

            AesCryptor cryptor = new AesGcmCryptor(getSecretKeyFromSecrets(secrets));
            cryptor.encrypt(inputStream, outputStream);

            log.info("Encryption finished successfully");
        } catch (GeneralSecurityException e) {
            throw new FileEncryptionException(e);
        }
    }
}
