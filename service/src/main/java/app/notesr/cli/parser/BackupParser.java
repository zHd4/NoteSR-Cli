package app.notesr.cli.parser;

import app.notesr.cli.core.security.dto.CryptoSecrets;
import app.notesr.cli.parser.v1.ParserV1;
import app.notesr.cli.parser.v2.ParserV2;
import app.notesr.cli.parser.v3.ParserV3;
import lombok.Getter;
import lombok.Setter;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import static app.notesr.cli.core.util.BackupValidator.isV1Format;
import static app.notesr.cli.core.util.BackupValidator.isV2Format;
import static app.notesr.cli.core.util.BackupValidator.isV3Format;

@Getter
public final class BackupParser implements Runnable {
    private final Path backupPath;
    private final Path outputDbPath;
    private final CryptoSecrets secrets;

    @Setter
    private Path tempDirPath;

    public BackupParser(Path backupPath, Path outputDbPath, CryptoSecrets secrets) {
        this.backupPath = backupPath;
        this.outputDbPath = outputDbPath;
        this.secrets = secrets;
        this.tempDirPath = Path.of(backupPath.toString() + "_temp");
    }

    @Override
    public void run() {
        String backupPathStr = backupPath.toString();

        List<Supplier<Parser>> parserSuppliers = List.of(
                () -> isV1Format(backupPathStr) ? new ParserV1(backupPath, outputDbPath) : null,
                () -> isV2Format(backupPathStr) ? new ParserV2(backupPath, tempDirPath, outputDbPath) : null,
                () -> isV3Format(backupPathStr) ? new ParserV3(backupPath, outputDbPath, secrets) : null
        );

        getTargetParser(parserSuppliers).parse();
    }

    private Parser getTargetParser(List<Supplier<Parser>> parserSuppliers) {
        for (Supplier<Parser> parserSupplier : parserSuppliers) {
            Parser parser = parserSupplier.get();

            if (parser != null) {
                return parser;
            }
        }

        throw new BackupParserException("Unsupported backup format");
    }
}
