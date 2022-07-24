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

    public JenkinsServer getServer() {
        try {
            if (username != null && apiToken != null)
                return new JenkinsServer(new URI(serverURL), username, apiToken);
            else return new JenkinsServer(new URI(serverURL));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
