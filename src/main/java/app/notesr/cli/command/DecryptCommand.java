package app.notesr.cli.command;

import app.notesr.cli.crypto.Aes;
import app.notesr.cli.crypto.BackupDecryptor;
import app.notesr.cli.crypto.CryptoKey;
import app.notesr.cli.crypto.CryptoKeyUtils;
import app.notesr.cli.crypto.exception.BackupDecryptionException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

@Getter
@CommandLine.Command(name = "decrypt",
        description = "Decrypts exported NoteSR .bak file and converts it to a SQLite database.")
public final class DecryptCommand implements Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(DecryptCommand.class);

    @CommandLine.Parameters(index = "0", paramLabel = "file_path", description = "path to encrypted NoteSR .bak file")
    private String encryptedBackupPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output SQLite database path")
    private String outputFilePath;

    @Override
    public Integer call() {
        File encryptedBackupFile;
        File keyFile;

        try {
            encryptedBackupFile = getFile(this.encryptedBackupPath);
            keyFile = getFile(this.keyPath);
        } catch (NoSuchFileException e) {
            LOGGER.error(e.getMessage());
            return 2;
        }

        CryptoKey cryptoKey;

        try {
            cryptoKey = getCryptoKey(keyFile);
        } catch (IOException e) {
            LOGGER.error("{}: an error occurred while reading", keyPath);
            return 2;
        }

        File tempDecryptedBackup;

        try {
            tempDecryptedBackup = decryptBackup(encryptedBackupFile, cryptoKey);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e); // Already validated
        } catch (BackupDecryptionException e) {
            LOGGER.error("{}: failed to decrypt, file corrupted or invalid key", encryptedBackupPath);
            return 7;
        }

        LOGGER.info("Decrypt!");
        return 0;
    }

    private File decryptBackup(File encryptedBackup, CryptoKey cryptoKey) throws FileNotFoundException,
            BackupDecryptionException {
        File decryptedBackup = Path.of(encryptedBackup.getAbsolutePath() + "_decrypted").toFile();

        BackupDecryptor decryptor = new BackupDecryptor(cryptoKey);
        FileInputStream inputStream = new FileInputStream(encryptedBackup);
        FileOutputStream outputStream = new FileOutputStream(decryptedBackup);

        decryptor.decrypt(inputStream, outputStream);
        return decryptedBackup;
    }

    private CryptoKey getCryptoKey(File keyFile) throws IOException {
        String hexKey = Files.readString(keyFile.toPath());
        return CryptoKeyUtils.hexToCryptoKey(hexKey, Aes.KEY_GENERATOR_ALGORITHM);
    }

    private File getFile(String path) throws NoSuchFileException {
        File file = new File(path);

        if (!file.exists() && !file.isFile()) {
            throw new NoSuchFileException(path + ": file not found");
        }

        return file;
    }
}
