package app.notesr.cli.command;

import app.notesr.cli.crypto.CryptoKey;
import app.notesr.cli.crypto.CryptoKeyUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import org.slf4j.Logger;

import static app.notesr.cli.crypto.FileCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.PathUtils.getNameWithoutExtension;
import static app.notesr.cli.util.Wiper.wipeDir;
import static app.notesr.cli.util.Wiper.wipeFile;
import static java.util.Objects.requireNonNullElseGet;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class Command implements Callable<Integer> {
    public static final int SUCCESS = 0;
    public static final int FILE_RW_ERROR = 2;
    public static final int DB_WRITING_ERROR = 5;
    public static final int UNKNOWN_ERROR = 6;
    public static final int DECRYPTION_ERROR = 7;

    private final Logger log;

    protected final CryptoKey getCryptoKey(File keyFile) throws CommandHandlingException, NumberFormatException {
        try {
            String hexKey = Files.readString(keyFile.toPath());
            return CryptoKeyUtils.hexToCryptoKey(hexKey, KEY_GENERATOR_ALGORITHM);
        } catch (NumberFormatException e) {
            log.error("{}: invalid key", keyFile.getAbsolutePath());
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (IOException e) {
            log.error("{}: an error occurred while reading", keyFile.getAbsolutePath());
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }

    protected final File getFile(String path) throws CommandHandlingException {
        File file = new File(path);

        if (!file.exists() && !file.isFile()) {
            log.error("{}: file not found", path);
            throw new CommandHandlingException(FILE_RW_ERROR);
        }

        return file;
    }

    protected final File getOutputFile(File encryptedBackupFile, String outputFilePath, String outputFileExtension)
            throws CommandHandlingException {
        File outputFile = Path.of(requireNonNullElseGet(outputFilePath, () ->
                getNameWithoutExtension(encryptedBackupFile) + outputFileExtension)).toFile();

        if (outputFile.exists()) {
            log.error("{}: file already exists", outputFile.getAbsolutePath());
            throw new CommandHandlingException(FILE_RW_ERROR);
        }

        return outputFile;
    }

    protected final void cleanTempFiles(File... files) throws CommandHandlingException {
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
}
