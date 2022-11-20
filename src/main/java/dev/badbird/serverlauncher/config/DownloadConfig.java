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
        System.out.println("[Downloader] Source isn't set, cannot download");
        //else throw new RuntimeException("Source is not set!");
    }

    public void download() {
        if (fileName.isEmpty()) throw new RuntimeException("File name is not set! If you want to download a directory from github, set it to '.', or the directory name");
        download(new File(fileName));
    }
}
