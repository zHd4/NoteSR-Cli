package app.notesr.cli.service;

import app.notesr.cli.crypto.FileCryptor;
import app.notesr.cli.crypto.FileEncryptionException;
import app.notesr.cli.dto.CryptoKey;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public final class BackupEncryptionService {
    public void encrypt(File inputFile, File outputFile, CryptoKey key)
            throws IOException, FileEncryptionException {

        try (FileInputStream input = new FileInputStream(inputFile);
             FileOutputStream output = new FileOutputStream(outputFile)) {

            log.info("Encrypting {}", inputFile.getAbsolutePath());

            FileCryptor fileCryptor = new FileCryptor(key);
            fileCryptor.encrypt(input, output);

            log.info("Encryption finished successfully");
        }
    }
}
