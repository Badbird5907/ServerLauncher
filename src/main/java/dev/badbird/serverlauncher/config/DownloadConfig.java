package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.config.source.GithubReleaseSource;
import dev.badbird.serverlauncher.config.source.JenkinsSource;
import dev.badbird.serverlauncher.config.source.TeamCitySource;
import dev.badbird.serverlauncher.config.source.URLSource;

import java.io.File;

public class DownloadConfig { // declare the variables please

    private JenkinsSource jenkins;
    private TeamCitySource teamCity;
    private URLSource url;
    private GithubReleaseSource github;

    public void download(File file) {
        if (jenkins != null) jenkins.download(file);
        else if (teamCity != null) teamCity.download(file);
        else if (github != null) github.download(file);
        else if (url != null) url.download(file);
        else throw new RuntimeException("Source is not set!");
    }
}
