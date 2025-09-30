package app.notesr.cli.service;

import app.notesr.cli.crypto.BackupCryptor;
import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.dto.CryptoKey;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import static app.notesr.cli.validation.BackupValidator.isValid;

public final class BackupDecryptionService {
    public File decrypt(File encryptedBackup, CryptoKey cryptoKey) throws FileDecryptionException, IOException {
        File decryptedBackup = new File(encryptedBackup.getAbsolutePath() + "_decrypted");

        try (FileInputStream inputStream = new FileInputStream(encryptedBackup);
             FileOutputStream outputStream = new FileOutputStream(decryptedBackup)) {

            BackupCryptor backupCryptor = new BackupCryptor(cryptoKey);
            backupCryptor.decrypt(inputStream, outputStream);
        }

        if (!isValid(decryptedBackup.getAbsolutePath())) {
            if (decryptedBackup.exists()) {
                Files.delete(decryptedBackup.toPath());
            }

            throw new FileDecryptionException();
        }

        return decryptedBackup;
    }
}
