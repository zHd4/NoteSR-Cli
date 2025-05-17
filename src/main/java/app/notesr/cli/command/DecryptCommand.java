package app.notesr.cli.command;

import app.notesr.cli.crypto.FileCryptor;
import app.notesr.cli.crypto.CryptoKey;
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

import static app.notesr.cli.validation.BackupValidator.isValid;

@Slf4j
@CommandLine.Command(name = "decrypt",
        description = "Decrypts exported NoteSR .bak file and converts it to a SQLite database.")
public final class DecryptCommand extends Command {
    @CommandLine.Parameters(index = "0", paramLabel = "file_path", description = "path to encrypted NoteSR .bak file")
    private String encryptedBackupPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output SQLite database path")
    private String outputFilePath;

    public DecryptCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        try {
            File encryptedBackupFile = getFile(encryptedBackupPath);
            File keyFile = getFile(keyPath);
            File outputFile = getOutputFile(encryptedBackupFile, outputFilePath, ".db");

            CryptoKey cryptoKey = getCryptoKey(keyFile);
            File tempDecryptedBackup = decryptBackup(encryptedBackupFile, cryptoKey);

            BackupParser parser = parseBackup(tempDecryptedBackup, outputFile);
            cleanupTemporaryFiles(tempDecryptedBackup, parser.getTempDirPath().toFile());

            log.info("Saved to: {}", outputFile.getAbsolutePath());
            return SUCCESS;
        } catch (CommandHandlingException e) {
            return e.getExitCode();
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
}
