package app.notesr.cli;

import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() {
        return new String[]{
            "Version: " + getUtilityVersion(),
            "Default NoteSR version: " + getDefaultNoteSrVersion()
        };
    }

    public String getUtilityVersion() {
        String version = getProperties().getProperty("utility.version");
        return version != null ? version : "unknown";
    }

    public String getDefaultNoteSrVersion() {
        String version = getProperties().getProperty("noteSr.defaultVersion");
        return version != null ? version : "unknown";
    }

    private Properties getProperties() {
        Properties props = new Properties();

        try (InputStream in = getClass().getResourceAsStream("/version.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return props;
    }
}
