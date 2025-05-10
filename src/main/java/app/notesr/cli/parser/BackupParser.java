package app.notesr.cli.parser;

import app.notesr.cli.exception.BackupIOException;
import app.notesr.cli.parser.v2.ParserV2;
import app.notesr.cli.parser.v1.ParserV1;
import app.notesr.cli.util.ZipUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.nio.file.Path;

@Getter
public final class BackupParser implements Runnable {
    private final Path backupPath;
    private final Path outputDbPath;

    @Setter
    private Path tempDirPath;

    public BackupParser(Path backupPath, Path outputDbPath) {
        this.backupPath = backupPath;
        this.outputDbPath = outputDbPath;
        this.tempDirPath = Path.of(backupPath.toString() + "_temp");
    }

    @Override
    public void run() {
        Parser targetParser;

        try {
            if (ZipUtils.isZipArchive(backupPath.toString())) {
                targetParser = new ParserV2(backupPath, tempDirPath, outputDbPath);
            } else {
                targetParser = new ParserV1(backupPath, outputDbPath);
            }
        } catch (IOException e) {
            throw new BackupIOException(e);
        }

        targetParser.run();
    }
}
