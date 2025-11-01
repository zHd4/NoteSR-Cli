package app.notesr.cli.command;

import app.notesr.cli.VersionProvider;

import app.notesr.cli.security.crypto.dto.CryptoSecrets;
import app.notesr.cli.exception.BackupEncryptionException;
import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.service.BackupCompilationService;
import app.notesr.cli.service.BackupEncryptionService;
import app.notesr.cli.service.workflow.BackupCompileWorkflow;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@CommandLine.Command(name = "compile",
        description = "Compiles NoteSR Backup Database into NoteSR .notesr.bak file.")
public final class CompileCommand extends Command {

    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = {"-o", "--output"}, description = "output file path")
    private String outputFilePath;

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
            CryptoSecrets secrets = getCryptoSecrets(keyFile);

            outputFile = getOutputFile(dbFile, outputFilePath != null ? Path.of(outputFilePath) : null,
                    ".notesr.bak");

            compile(dbFile, secrets, outputFile, tempFiles);
            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            if (outputFile != null && outputFile.exists()) {
                tempFiles.add(outputFile);
            }

            exitCode = e.getExitCode();
        } finally {
            cleanTempFiles(tempFiles);

            if (outputFile != null && outputFile.exists() && exitCode == SUCCESS) {
                log.info("Saved to: {}", outputFile.getAbsolutePath());
            }
        }

        return exitCode;
    }

    private void compile(File dbFile, CryptoSecrets secrets, File outputFile, List<File> tempFiles)
            throws CommandHandlingException {
        try {
            BackupEncryptionService encryptionService = new BackupEncryptionService();
            BackupCompilationService compilationService = new BackupCompilationService();

            BackupCompileWorkflow workflow = new BackupCompileWorkflow(compilationService, encryptionService);

            File tempArchive = getOutputFile(dbFile, null, ".zip");
            tempFiles.add(tempArchive);

            workflow.run(dbFile, tempArchive, outputFile, secrets, getNoteSrVersion());
        } catch (BackupIOException e) {
            log.error("{}: failed to compile, details:\n{}", dbPath, e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (BackupEncryptionException e) {
            log.error("{}: failed to encrypt, key may be invalid", dbPath);
            log.debug("E: ", e);
            throw new CommandHandlingException(CRYPTO_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            log.debug("E: ", e);
            throw new CommandHandlingException(UNKNOWN_ERROR);
        }
    }

    private String getNoteSrVersion() {
        VersionProvider versionProvider = new VersionProvider();
        return versionProvider.getNoteSrVersion();
    }
}
