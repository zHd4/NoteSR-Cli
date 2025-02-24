package app.notesr.cli.parser.v2;

import app.notesr.cli.parser.BackupIOException;
import app.notesr.cli.parser.Parser;
import app.notesr.cli.util.ZipUtils;

import java.io.IOException;
import java.nio.file.Path;

public class ParserV2 extends Parser {
    private final Path tempDirPath;

    public ParserV2(Path backupPath, Path tempDirPath, Path outputDbPath) {
        super(backupPath, outputDbPath);
        this.tempDirPath = tempDirPath;
    }

    @Override
    public void run() {
        try {
            ZipUtils.unzip(backupPath.toString(), tempDirPath.toString());
        } catch (IOException e) {
            throw new BackupIOException(e);
        }
    }
}
