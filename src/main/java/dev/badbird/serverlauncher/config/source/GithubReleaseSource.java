package dev.badbird.serverlauncher.config.source;

import dev.badbird.serverlauncher.util.Utilities;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

public class GithubReleaseSource implements DownloadSource {
    private String username, token;

    private String repository, release, asset;

    @Override
    public void download(File file) {
        try {
            GitHub github;
            if (token != null && !token.isEmpty() && username != null && !username.isEmpty()) {
                github = GitHub.connect(username, token);
            } else if (token != null && !token.isEmpty()) {
                github = GitHub.connectUsingOAuth(token);
            } else {
                github = GitHub.connectAnonymously();
            }
            GHRelease rel;
            GHRepository repo = github.getRepository(repository.toLowerCase().replace("https://github.com/", ""));

            if (release != null && !release.isEmpty()) {
                rel = repo.getReleaseByTagName(release);
            } else {
                rel = repo.getLatestRelease();
            }
            Pattern pattern = Pattern.compile(asset);
            for (GHAsset ghAsset : rel.listAssets().toList()) {
                if (pattern.matcher(ghAsset.getName()).matches() || ghAsset.getName().equalsIgnoreCase(asset)) {
                    Utilities.downloadFile(file, ghAsset.getBrowserDownloadUrl());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
