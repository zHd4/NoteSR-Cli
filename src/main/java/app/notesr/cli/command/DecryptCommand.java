package app.notesr.cli.command;

import app.notesr.cli.crypto.FileCryptor;
import app.notesr.cli.crypto.CryptoKey;
import app.notesr.cli.crypto.CryptoKeyUtils;
import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.parser.BackupParser;
import app.notesr.cli.parser.BackupParserException;
import app.notesr.cli.parser.UnexpectedFieldException;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.PathUtils.getNameWithoutExtension;
import static app.notesr.cli.validation.BackupValidator.isValid;
import static app.notesr.cli.util.Wiper.wipeDir;
import static app.notesr.cli.util.Wiper.wipeFile;
import static java.util.Objects.requireNonNullElseGet;

@Slf4j
@CommandLine.Command(name = "decrypt",
        description = "Decrypts exported NoteSR .bak file and converts it to a SQLite database.")
public final class DecryptCommand extends Command {
    public static final int SUCCESS = 0;
    public static final int FILE_RW_ERROR = 2;
    public static final int DB_CONNECTION_ERROR = 3;
    public static final int DB_WRITING_ERROR = 5;
    public static final int UNKNOWN_ERROR = 6;
    public static final int DECRYPTION_ERROR = 7;

    @CommandLine.Parameters(index = "0", paramLabel = "file_path", description = "path to encrypted NoteSR .bak file")
    private String encryptedBackupPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output SQLite database path")
    private String outputFilePath;

    @Override
    public Integer call() {
        try {
            File encryptedBackupFile = getEncryptedBackupFile();
            File keyFile = getKeyFile();
            File outputFile = getOutputFile(encryptedBackupFile);

            CryptoKey cryptoKey = readCryptoKey(keyFile);
            File tempDecryptedBackup = decryptBackup(encryptedBackupFile, cryptoKey);

            BackupParser parser = parseBackup(tempDecryptedBackup, outputFile);
            cleanupTemporaryFiles(tempDecryptedBackup, parser.getTempDirPath().toFile());

            log.info("Saved to: {}", outputFile.getAbsolutePath());
            return SUCCESS;
        } catch (CommandHandlingException e) {
            return e.getExitCode();
        }
    }

    private File getEncryptedBackupFile() throws CommandHandlingException {
        try {
            return getFile(this.encryptedBackupPath);
        } catch (NoSuchFileException e) {
            log.error(e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }

    private File getKeyFile() throws CommandHandlingException {
        try {
            return getFile(this.keyPath);
        } catch (NoSuchFileException e) {
            log.error(e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }

    private File getOutputFile(File encryptedBackupFile) throws CommandHandlingException {
        File outputFile = Path.of(requireNonNullElseGet(outputFilePath, () ->
                getNameWithoutExtension(encryptedBackupFile) + ".db")).toFile();

        if (outputFile.exists()) {
            log.error("{}: file already exists", outputFile.getAbsolutePath());
            throw new CommandHandlingException(DB_CONNECTION_ERROR);
        }

        return outputFile;
    }

    private CryptoKey readCryptoKey(File keyFile) throws CommandHandlingException {
        try {
            return getCryptoKey(keyFile);
        } catch (NumberFormatException e) {
            log.error("{}: invalid key", keyPath);
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (IOException e) {
            log.error("{}: an error occurred while reading", keyPath);
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }

    private File decryptBackup(File encryptedBackup, CryptoKey cryptoKey) throws CommandHandlingException {
        log.info("Decrypting {}", encryptedBackupPath);
        File decryptedBackup = new File(encryptedBackup.getAbsolutePath() + "_decrypted");

        try {
            FileCryptor decryptor = new FileCryptor(cryptoKey);
            FileInputStream inputStream = new FileInputStream(encryptedBackup);
            FileOutputStream outputStream = new FileOutputStream(decryptedBackup);

            decryptor.decrypt(inputStream, outputStream);

            if (!isValid(decryptedBackup.getAbsolutePath())) {
                throw new FileDecryptionException();
            }

            log.info("Decryption finished successfully");
            return decryptedBackup;
        } catch (FileDecryptionException e) {
            if (decryptedBackup.exists()) {
                try {
                    Files.delete(decryptedBackup.toPath());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            log.error("{}: failed to decrypt, invalid key or file corrupted", encryptedBackupPath);
            throw new CommandHandlingException(DECRYPTION_ERROR);
        }  catch (IOException e) {
            throw new RuntimeException(e); // Already validated
        }
    }

    private BackupParser parseBackup(File tempDecryptedBackup, File outputFile) throws CommandHandlingException {
        try {
            log.info("Parsing {}", encryptedBackupPath);
            BackupParser parser = new BackupParser(tempDecryptedBackup.toPath(), outputFile.toPath());
            parser.run();
            log.info("Parsing finished successfully");
            return parser;
        } catch (BackupIOException | BackupParserException | UnexpectedFieldException e) {
            log.error("{}: failed to parse, details:\n{}", encryptedBackupPath, e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (BackupDbException e) {
            log.error("Failed to write data to database, details:\n{}", e.getMessage());
            throw new CommandHandlingException(DB_WRITING_ERROR);
        }
    }

    private void cleanupTemporaryFiles(File... files) throws CommandHandlingException {
        try {
            log.info("Cleaning temporary files");

            for (File file : files) {
                if (file.exists()) {
                    if (file.isFile()) {
                        wipeFile(file);
                    } else {
                        wipeDir(file);
                    }
                }
            }

            log.info("Cleaning finished successfully");
        } catch (IOException e) {
            log.error("Unknown error, details:\n{}", e.getMessage());
            throw new CommandHandlingException(UNKNOWN_ERROR);
        }
    }

    private CryptoKey getCryptoKey(File keyFile) throws IOException, NumberFormatException {
        String hexKey = Files.readString(keyFile.toPath());
        return CryptoKeyUtils.hexToCryptoKey(hexKey, KEY_GENERATOR_ALGORITHM);
    }

    private File getFile(String path) throws NoSuchFileException {
        File file = new File(path);

        if (!file.exists() && !file.isFile()) {
            throw new NoSuchFileException(path + ": file not found");
        }

        return file;
    }
}
