package app.notesr.cli.service;

import app.notesr.cli.security.crypto.AesCbcCryptor;
import app.notesr.cli.security.crypto.AesGcmCryptor;
import app.notesr.cli.exception.BackupDecryptionException;
import app.notesr.cli.security.crypto.dto.CryptoSecrets;
import app.notesr.cli.exception.BackupIOException;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.nio.file.Files;
import java.security.GeneralSecurityException;

import static app.notesr.cli.util.KeyUtils.getIvFromSecrets;
import static app.notesr.cli.util.KeyUtils.getSecretKeyFromSecrets;
import static app.notesr.cli.util.BackupValidator.isValid;

@Slf4j
public final class BackupDecryptionService {
    public File decrypt(File encryptedBackup, CryptoSecrets secrets) throws BackupDecryptionException, IOException {
        File decryptedBackup = new File(encryptedBackup.getAbsolutePath() + "_decrypted");

        try (FileInputStream inputStream = new FileInputStream(encryptedBackup);
             FileOutputStream outputStream = new FileOutputStream(decryptedBackup)) {
            tryGcmDecryption(inputStream, outputStream, secrets);
            deleteAndThrowIfInvalid(decryptedBackup);
            return decryptedBackup;
        } catch (GeneralSecurityException e) {
            log.debug("GCM decryption failed", e);
        }

        try (FileInputStream inputStream = new FileInputStream(encryptedBackup);
             FileOutputStream outputStream = new FileOutputStream(decryptedBackup)) {
            tryCbcDecryption(inputStream, outputStream, secrets);
            deleteAndThrowIfInvalid(decryptedBackup);
        } catch (GeneralSecurityException e) {
            log.debug("CBC decryption failed", e);
            throw new BackupDecryptionException(e);
        }

        return decryptedBackup;
    }

    private void tryGcmDecryption(InputStream sourceStream, FileOutputStream outputStream, CryptoSecrets secrets)
            throws GeneralSecurityException, IOException {
        SecretKey key = getSecretKeyFromSecrets(secrets);
        AesGcmCryptor cryptor = new AesGcmCryptor(key);
        cryptor.decrypt(sourceStream, outputStream);
    }

    private void tryCbcDecryption(InputStream sourceStream, FileOutputStream outputStream, CryptoSecrets secrets)
            throws GeneralSecurityException, IOException {
        SecretKey key = getSecretKeyFromSecrets(secrets);
        byte[] iv = getIvFromSecrets(secrets);

        AesCbcCryptor cryptor = new AesCbcCryptor(key, iv);
        cryptor.decrypt(sourceStream, outputStream);
    }

    private void deleteAndThrowIfInvalid(File backupFile) throws BackupDecryptionException, IOException {
        try {
            if (!isValid(backupFile.getAbsolutePath())) {
                if (backupFile.exists()) {
                    Files.delete(backupFile.toPath());
                }

                throw new BackupDecryptionException("Decrypted file is not a valid backup");
            }
        } catch (BackupIOException e) {
            throw e.getCause();
        }
    }
}
