package app.notesr.cli;

import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        String version = App.class.getPackage().getImplementationVersion();
        return new String[] {
                "NoteSR-Cli Version: " + (version != null ? version : "unknown" )
        };
    }
}
