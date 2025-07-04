package app.notesr.cli.command;

import app.notesr.cli.VersionProvider;
import app.notesr.cli.crypto.FileEncryptionException;
import app.notesr.cli.dto.CryptoKey;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.service.BackupCompilationService;
import app.notesr.cli.service.BackupEncryptionService;
import app.notesr.cli.service.workflow.BackupCompileWorkflow;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Slf4j
@CommandLine.Command(name = "compile",
        description = "Compiles NoteSR Backup Database into NoteSR .bak file.")
public final class CompileCommand extends Command {

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output file path")
    private String outputFilePath;

    @CommandLine.Option(names = { "-n", "--notesr-version" }, description = "target NoteSR version "
            + "(see --version to check default version)")
    private String noteSrVersion;

    public CompileCommand() {
        super(log);
    }

    @Override
    public Integer call() {
        int exitCode = UNKNOWN_ERROR;
        File outputFile = null;
        List<File> tempFiles = new ArrayList<>();

        try {
            File dbFile = getFile(dbPath);
            File keyFile = getFile(keyPath);
            CryptoKey key = getCryptoKey(keyFile);

            outputFile = getOutputFile(dbFile, outputFilePath != null ? Path.of(outputFilePath) : null,
                    ".notesr.bak");

            compile(dbFile, key, outputFile, tempFiles);
            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        } finally {
            cleanTempFiles(tempFiles);
            if (outputFile != null && exitCode == SUCCESS) {
                log.info("Saved to: {}", outputFile.getAbsolutePath());
            }
        }

        return exitCode;
    }

    private void compile(File dbFile, CryptoKey key, File outputFile, List<File> tempFiles)
            throws CommandHandlingException {
        try {
            BackupEncryptionService encryptionService = new BackupEncryptionService();
            BackupCompilationService compilationService = new BackupCompilationService();

            BackupCompileWorkflow workflow = new BackupCompileWorkflow(compilationService, encryptionService);

            File tempArchive = getOutputFile(dbFile, null, ".zip");
            tempFiles.add(tempArchive);

            Path tempDir = workflow.run(dbFile, tempArchive, outputFile, key, getNoteSrVersion());
            tempFiles.add(tempDir.toFile());
        } catch (BackupIOException e) {
            log.error("{}: failed to compile, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);

        } catch (BackupDbException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);

        } catch (FileEncryptionException e) {
            log.error("{}: failed to encrypt, key may be invalid", dbPath);
            throw new CommandHandlingException(CRYPTO_ERROR);

        } catch (IOException e) {
            log.error("I/O error: {}", e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);

        } catch (Exception e) {
            log.error("Unexpected error", e);
            throw new CommandHandlingException(UNKNOWN_ERROR);
        }
    }

    private String getNoteSrVersion() {
        return requireNonNullElseGet(noteSrVersion, () -> {
            VersionProvider versionProvider = new VersionProvider();
            return versionProvider.getDefaultNoteSrVersion();
        });
    }
}
