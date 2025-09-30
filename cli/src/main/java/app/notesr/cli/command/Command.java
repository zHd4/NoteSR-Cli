package app.notesr.cli.command;

import app.notesr.cli.dto.CryptoKey;
import app.notesr.cli.util.CryptoKeyUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import static app.notesr.cli.crypto.BackupCryptor.KEY_GENERATOR_ALGORITHM;
import static app.notesr.cli.util.FileUtils.getNameWithoutExtension;
import static app.notesr.cli.util.Wiper.wipeDir;
import static app.notesr.cli.util.Wiper.wipeFile;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
abstract class Command implements Callable<Integer> {
    public static final int SUCCESS = 0;
    public static final int FILE_RW_ERROR = 2;
    public static final int DB_ERROR = 5;
    public static final int UNKNOWN_ERROR = 6;
    public static final int CRYPTO_ERROR = 7;

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

    protected final File getOutputFile(File inputFile, Path outputFilePath, String outputFileExtension)
            throws CommandHandlingException {
        File outputFile = null;

        try {
            if (outputFilePath == null) {
                String outputFileName = getNameWithoutExtension(inputFile) + outputFileExtension;
                outputFile = new File(inputFile.getParentFile(), outputFileName);
            } else if (Files.isDirectory(outputFilePath)) {
                String outputFileName = getNameWithoutExtension(inputFile) + outputFileExtension;
                outputFile = outputFilePath.resolve(outputFileName).toFile();
            } else if (Files.exists(outputFilePath)) {
                throw new FileAlreadyExistsException(outputFilePath.toString());
            } else {
                outputFile = outputFilePath.toFile();
            }

            if (outputFile.exists()) {
                throw new FileAlreadyExistsException(outputFile.getAbsolutePath());
            }
        } catch (FileAlreadyExistsException e) {
            log.error("{}: file already exists", outputFile != null ? outputFile.getAbsolutePath() : outputFilePath);
            throw new CommandHandlingException(FILE_RW_ERROR);
        }

        return outputFile;
    }

    protected final void cleanTempFiles(List<File> files) {
        if (files.isEmpty()) {
            return;
        }

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
        }
    }

    static String truncateText(String text, int maxLength) {
        if (text.length() < maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 1) + "…";
    }
}
