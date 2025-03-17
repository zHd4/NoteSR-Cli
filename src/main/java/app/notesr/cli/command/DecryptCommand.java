package app.notesr.cli.command;

import lombok.Getter;
import picocli.CommandLine;

@Getter
@CommandLine.Command(name = "decrypt",
        description = "Decrypts exported NoteSR .bak file and converts it to a SQLite database.")
public final class DecryptCommand implements Command {
    @CommandLine.Parameters(index = "0", paramLabel = "file_path", description = "path to encrypted NoteSR .bak file")
    private String encryptedBackupPath;

    @CommandLine.Parameters(index = "1", paramLabel = "key_path", description = "path to exported key (text file)")
    private String keyPath;

    @CommandLine.Option(names = { "-o", "--output" }, description = "output SQLite database path")
    private String outputFilePath;

    @Override
    public Integer call() {
        System.out.println("Decrypt!");
        return 0;
    }
}
