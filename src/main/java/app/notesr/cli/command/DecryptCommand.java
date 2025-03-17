package app.notesr.cli.command;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.NoSuchFileException;

@Getter
@CommandLine.Command(name = "decrypt",
        description = "Decrypts exported NoteSR .bak file and converts it to a SQLite database.")
public final class DecryptCommand implements Command {
    private static final Logger logger = LoggerFactory.getLogger(DecryptCommand.class);

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
            logger.error(e.getMessage());
            return 1;
        }

        logger.info("Decrypt!");
        return 0;
    }

    private File getFile(String path) throws NoSuchFileException {
        File file = new File(path);

        if (!file.exists() && !file.isFile()) {
            throw new NoSuchFileException(path + ": file not found");
        }

        return file;
    }
}
