package app.notesr.cli.parser;

import lombok.RequiredArgsConstructor;

import java.nio.file.Path;

@RequiredArgsConstructor
public abstract class Parser implements Runnable {
    protected final Path backupPath;
    protected final Path outputDbPath;
}
