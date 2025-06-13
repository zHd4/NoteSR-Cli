package app.notesr.cli;

import app.notesr.cli.command.CompileCommand;
import app.notesr.cli.command.DecryptCommand;
import app.notesr.cli.command.GetFileCommand;
import app.notesr.cli.command.ListFilesCommand;
import app.notesr.cli.command.ListNotesCommand;
import app.notesr.cli.command.PutFileCommand;
import app.notesr.cli.command.ReadNoteCommand;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "notesr-cli",
        versionProvider = VersionProvider.class,
        description = "Decrypts and manages exported NoteSR backups",
        mixinStandardHelpOptions = true,
        subcommands = {DecryptCommand.class, CompileCommand.class, ListNotesCommand.class, ReadNoteCommand.class,
            ListFilesCommand.class, GetFileCommand.class, PutFileCommand.class})
public final class App implements Callable<Integer> {
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        CommandLine cmd = new CommandLine(new App());

        cmd.setUnmatchedArgumentsAllowed(true);
        cmd.setExecutionStrategy(parseResult -> {
            if (parseResult.isUsageHelpRequested()) {
                printUsage(cmd);
                return 0;
            }

            return new CommandLine.RunLast().execute(parseResult);
        });

        cmd.setExecutionExceptionHandler((ex, commandLine, parseResult) -> {
            if (ex instanceof CommandLine.UnmatchedArgumentException) {
                System.err.println("Unknown command or argument: " + ex.getMessage());
            } else {
                System.err.println("Error: " + ex.getMessage());
            }

            return commandLine.getCommandSpec().exitCodeOnInvalidInput();
        });

        int exitCode;

        try {
            exitCode = cmd.execute(args);
        } catch (CommandLine.UnmatchedArgumentException ex) {
            System.err.println("Unknown command or argument: " + ex.getMessage());
            printUsage(cmd);

            exitCode = cmd.getCommandSpec().exitCodeOnInvalidInput();
        }

        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        printUsage(spec.commandLine());
        return 0;
    }

    public static void printUsage(CommandLine cmd) {
        cmd.usage(System.out);
    }
}
