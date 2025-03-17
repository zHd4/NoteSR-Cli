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
    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        System.out.println("Please use -h flag to see the available commands.");
        System.out.println("No command provided. Exit.");
        return 0;
    }
}
