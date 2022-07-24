package dev.badbird.serverlauncher.config;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Artifact;
import com.offbytwo.jenkins.model.Build;
import lombok.Getter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

@Getter
public class PluginConfig {
    private String fileName;
    private String directDownload;
    private JenkinsConfig jenkinsConfig;

    public void download() {
        File file = new File("plugins", fileName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (directDownload != null) {
            System.out.println("[Plugin Downloader] Downloading " + fileName + " from " + directDownload);
            try {
                URL url = new URL(directDownload);
                InputStream in = url.openStream();
                downloadFile(file, in);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (jenkinsConfig != null) {
            JenkinsServer server = jenkinsConfig.getServer();
            System.out.println("[Plugin Downloader] Downloading " + fileName + " from Jenkins ");
            try {
                Build build = server.getJob(jenkinsConfig.getJobName())
                        .getLastSuccessfulBuild();
                Artifact artifact = null;
                for (Artifact artifact1 : build.details().getArtifacts()) {
                    if (artifact1.getFileName().equalsIgnoreCase(jenkinsConfig.getArtifactName())) {
                        artifact = artifact1;
                    }
                }
                if (artifact != null) {
                    InputStream is = build.details().downloadArtifact(artifact);
                    if (file.exists()) file.delete();
                    //download the file
                    downloadFile(file, is);
                }
            } catch (IOException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void downloadFile(File file, InputStream is) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
    }
}
