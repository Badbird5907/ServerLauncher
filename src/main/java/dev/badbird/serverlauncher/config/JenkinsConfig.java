package dev.badbird.serverlauncher.config;

import com.offbytwo.jenkins.JenkinsServer;
import lombok.Getter;
import lombok.Setter;

import java.net.URI;
import java.net.URISyntaxException;

@Getter
@Setter
public class JenkinsConfig {
    private String serverURL;
    private String username;
    private String apiToken;
    private String jobName;
    private String artifactName;

    private transient JenkinsServer server;

    public JenkinsServer getServer() {
        if (server != null) return server;
        try {
            if (username != null && apiToken != null)
                return server = new JenkinsServer(new URI(serverURL), username, apiToken);
            else return server = new JenkinsServer(new URI(serverURL));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
