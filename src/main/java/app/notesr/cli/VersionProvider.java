package app.notesr.cli;

import picocli.CommandLine;

public final class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        String version = App.class.getPackage().getImplementationVersion();
        return new String[] {"Version: " + (version != null ? version : "unknown")};
    }
}
