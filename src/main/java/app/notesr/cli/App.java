package app.notesr.cli;

import app.notesr.cli.command.CompileCommand;
import app.notesr.cli.command.DecryptCommand;
import app.notesr.cli.command.ListNotesCommand;
import app.notesr.cli.command.ReadNoteCommand;
import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "notesr-cli",
        versionProvider = VersionProvider.class,
        description = "Decrypts and manages exported NoteSR backups",
        mixinStandardHelpOptions = true,
        subcommands = {DecryptCommand.class, CompileCommand.class, ListNotesCommand.class, ReadNoteCommand.class})
public final class App implements Callable<Integer> {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        throw new CommandLine.ParameterException(spec.commandLine(), "No command provided.");
    }
}
