package app.notesr.cli.service;

import app.notesr.cli.compiler.BackupCompiler;
import app.notesr.cli.exception.BackupDbException;
import app.notesr.cli.exception.BackupIOException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;

@Slf4j
public final class BackupCompilationService {
    public Path compile(File dbFile, File outputFile, String noteSrVersion)
            throws BackupIOException, BackupDbException {

        log.info("Compiling {}", dbFile.getAbsolutePath());
        BackupCompiler compiler = new BackupCompiler(dbFile.toPath(), outputFile.toPath(), noteSrVersion);
        compiler.run();

        log.info("Compiling finished successfully");
        return compiler.getTempDirPath();
    }
}
