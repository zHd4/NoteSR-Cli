package app.notesr.cli.command;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

@Slf4j
@CommandLine.Command(name = "put-file",
        description = "Attaches a file to a specific note stored in a NoteSR Backup Database.")
public final class PutFileCommand extends Command {
    @CommandLine.Parameters(index = "0", paramLabel = "db_path",
            description = "path to NoteSR Backup Database")
    private String dbPath;

    @CommandLine.Parameters(index = "1", paramLabel = "note_id",
            description = "note id")
    private String noteId;

    @CommandLine.Parameters(index = "2", paramLabel = "file_path",
            description = "path to file to attach")
    private String filePath;

    public PutFileCommand() {
        super(log);
    }

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}
