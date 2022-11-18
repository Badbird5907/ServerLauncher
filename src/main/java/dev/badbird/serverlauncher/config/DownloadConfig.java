package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.config.source.*;

import java.io.File;

public class DownloadConfig {
    private String fileName;
    private JenkinsSource jenkins;
    private TeamCitySource teamCity;
    private URLSource url;
    private GithubReleaseSource github;

    private GithubFileSource githubFile;

    public void download(File file) {
        if (jenkins != null) jenkins.download(file);
        else if (teamCity != null) teamCity.download(file);
        else if (github != null) github.download(file);
        else if (githubFile != null) githubFile.download(file);
        else if (url != null) url.download(file);
        else throw new RuntimeException("Source is not set!");
    }
    public void download() {
        download(new File(fileName));
    }
}
