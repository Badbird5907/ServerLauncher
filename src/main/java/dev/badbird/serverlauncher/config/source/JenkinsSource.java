package dev.badbird.serverlauncher.config.source;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Artifact;
import com.offbytwo.jenkins.model.Build;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

@Getter
@Setter
public class JenkinsSource implements DownloadSource {

    private String serverURL;
    private String username;
    private String token;
    private String jobName;
    private String artifactName;

    private @Deprecated String apiToken; // why the hell you depreciating it, then using it.
    private transient JenkinsServer server;

    public JenkinsServer getServer() {
        if (server != null) return server;

        if (apiToken != null && !apiToken.isEmpty()) {
            System.err.println("apiToken is deprecated, use token instead! (JenkinsSource)");
            token = apiToken;
        }

        try {
            if (username != null && token != null)
                return server = new JenkinsServer(new URI(serverURL), username, token);
            else return server = new JenkinsServer(new URI(serverURL));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void download(File file) {
        JenkinsServer server = getServer();
        System.out.println("[Downloader] Downloading " + file.getName() + " from Jenkins ");
        try {
            Build build = server.getJob(getJobName()).getLastSuccessfulBuild();
            Artifact artifact = null;

            for (Artifact artifact1 : build.details().getArtifacts()) {
                if (artifact1.getFileName().equalsIgnoreCase(getArtifactName())) {
                    artifact = artifact1;
                }
            }

            if (artifact != null) {
                InputStream is = build.details().downloadArtifact(artifact);
                Utilities.downloadFile(file, is);
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
