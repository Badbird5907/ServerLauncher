package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.config.source.*;
import lombok.Getter;

import java.io.File;

public class DownloadConfig {
    @Getter
    private String fileName;
    private JenkinsSource jenkins;
    private TeamCitySource teamCity;
    private URLSource url;
    private GithubReleaseSource github;
    private GithubFileSource githubFile;

    public void download(File file) {
        if (jenkins != null)
            jenkins.preDownload(file);
        else if (teamCity != null)
            teamCity.preDownload(file);
        else if (github != null)
            github.preDownload(file);
        else if (githubFile != null)
            githubFile.preDownload(file);
        else if (url != null)
            url.preDownload(file);
        else
            System.out.println("[Downloader] Source isn't set, cannot download");
        // else throw new RuntimeException("Source is not set!");
    }

    public void download() {
        if (fileName.isEmpty())
            throw new RuntimeException(
                    "File name is not set! If you want to download a directory from github, set it to '.', or the directory name");
        download(new File(fileName));
    }
}
