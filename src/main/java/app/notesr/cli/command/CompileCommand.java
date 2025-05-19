package app.notesr.cli.command;

import app.notesr.cli.VersionProvider;
import app.notesr.cli.compiler.BackupCompiler;
import app.notesr.cli.crypto.CryptoKey;
import app.notesr.cli.crypto.FileCryptor;
import app.notesr.cli.crypto.FileEncryptionException;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElseGet;

@Slf4j
@CommandLine.Command(name = "compile",
        description = "Compiles NoteSR SQLite database into NoteSR .bak file.")
public final class CompileCommand extends Command {

    @CommandLine.Parameters(index = "0", paramLabel = "db_path", description = "path to decrypted database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output file path")
    private String outputPath;

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
            outputFile = getOutputFile(dbFile, outputPath != null ? Path.of(outputPath) : null,
                    ".notesr.bak");

            File keyFile = getFile(keyPath);
            CryptoKey cryptoKey = getCryptoKey(keyFile);

            File tempArchiveFile = getOutputFile(dbFile, null, ".zip");
            tempFiles.add(tempArchiveFile);

            Path tempDirPath = compileBackup(dbFile, tempArchiveFile);
            tempFiles.add(tempDirPath.toFile());

            encryptBackup(tempArchiveFile, outputFile, cryptoKey);

            exitCode = SUCCESS;
        } catch (CommandHandlingException e) {
            exitCode = e.getExitCode();
        } finally {
            if (!tempFiles.isEmpty()) {
                cleanTempFiles(tempFiles);
            }

            if (outputFile != null && exitCode == SUCCESS) {
                log.info("Saved to: {}", outputFile.getAbsolutePath());
            }
        }

        return exitCode;
    }

    private Path compileBackup(File dbFile, File outputFile) throws CommandHandlingException {
        try {
            log.info("Compiling {}", dbPath);

            BackupCompiler backupCompiler = new BackupCompiler(dbFile.toPath(), outputFile.toPath(),
                    getNoteSrVersion());
            backupCompiler.run();

            log.info("Compiling finished successfully");
            return backupCompiler.getTempDirPath();
        } catch (BackupIOException e) {
            log.error("{}: failed to compile, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        } catch (BackupDbException e) {
            log.error("{}: failed to fetch data from database, details:\n{}", dbPath, e.getMessage());
            throw new CommandHandlingException(DB_ERROR);
        }
    }

    private void encryptBackup(File tempArchiveFile, File outputFile, CryptoKey cryptoKey)
            throws CommandHandlingException {
        log.info("Encrypting {}", dbPath);

        try {
            FileCryptor fileCryptor = new FileCryptor(cryptoKey);
            FileInputStream inputStream = new FileInputStream(tempArchiveFile);
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            fileCryptor.encrypt(inputStream, outputStream);
            log.info("Encryption finished successfully");
        } catch (FileEncryptionException e) {
            log.error("{}: failed to encrypt, key may be invalid", dbPath);
            throw new CommandHandlingException(CRYPTO_ERROR);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new CommandHandlingException(FILE_RW_ERROR);
        }
    }

    private String getNoteSrVersion() {
        return requireNonNullElseGet(noteSrVersion, () -> {
            VersionProvider versionProvider = new VersionProvider();
            return versionProvider.getDefaultNoteSrVersion();
        });
    }
}
