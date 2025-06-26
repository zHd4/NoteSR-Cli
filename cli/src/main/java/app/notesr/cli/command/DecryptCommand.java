package app.notesr.cli.command;

import app.notesr.cli.crypto.FileDecryptionException;
import app.notesr.cli.dto.CryptoKey;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.parser.BackupParserException;
import app.notesr.cli.parser.UnexpectedFieldException;
import app.notesr.cli.service.BackupParsingService;
import app.notesr.cli.service.DecryptWorkflow;
import app.notesr.cli.service.BackupDecryptionService;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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
        int exitCode;

        File outputFile;
        List<File> tempFiles = new ArrayList<>();

        try {
            File encryptedFile = getFile(encryptedBackupPath);
            File keyFile = getFile(keyPath);
            outputFile = getOutputFile(encryptedFile,
                    outputFilePath != null ? Path.of(outputFilePath) : null, ".notesr.db");

            decrypt(encryptedFile, keyFile, outputFile, tempFiles);
            exitCode = SUCCESS;

            log.info("Saved to: {}", outputFile.getAbsolutePath());
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        } finally {
            cleanTempFiles(tempFiles);
        }

        return exitCode;
    }

    private void decrypt(File encryptedBackupFile, File keyFile, File outputDbFile, List<File> tempFiles)
            throws CommandHandlingException {
        try {
            CryptoKey cryptoKey = getCryptoKey(keyFile);

            BackupDecryptionService backupDecryptionService = new BackupDecryptionService();
            BackupParsingService backupParsingService = new BackupParsingService();

            DecryptWorkflow workflow = new DecryptWorkflow(backupDecryptionService, backupParsingService);
            workflow.run(encryptedBackupFile, cryptoKey, outputDbFile, tempFiles);
        } catch (FileDecryptionException e) {
            log.error("{}: failed to decrypt, invalid key or file corrupted", encryptedBackupPath);
            throw new CommandHandlingException(CRYPTO_ERROR);
        } catch (BackupIOException | BackupParserException | UnexpectedFieldException e) {
            log.error("{}: failed to parse, details:\n{}", encryptedBackupPath, e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (BackupDbException e) {
            log.error("Failed to write data to database, details:\n{}", e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }
}
