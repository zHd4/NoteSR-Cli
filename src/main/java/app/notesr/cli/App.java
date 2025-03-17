package app.notesr.cli;

import app.notesr.cli.command.DecryptCommand;
import picocli.CommandLine;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "notesr-cli",
        versionProvider = VersionProvider.class,
        description = "Decrypts and manages exported NoteSR backups",
        mixinStandardHelpOptions = true,
        subcommands = {DecryptCommand.class})
public final class App implements Callable<Integer> {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        throw new CommandLine.ParameterException(spec.commandLine(), "No command provided.");
    }
}
