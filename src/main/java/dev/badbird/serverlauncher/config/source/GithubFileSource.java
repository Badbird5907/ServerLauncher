package dev.badbird.serverlauncher.config.source;

import dev.badbird.serverlauncher.util.Utilities;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;

public class GithubFileSource implements DownloadSource {
    private String username, token;

    private String repository, path, branch = "master";

    @Override
    public void download(File file) {

        try {
            GitHub github;
            System.out.println("Downloading from Github, repository: " + repository + ", path: " + path + ", branch: " + branch);
            if (token != null && !token.isEmpty() && username != null && !username.isEmpty()) {
                github = GitHub.connect(username, token);
            } else if (token != null && !token.isEmpty()) {
                github = GitHub.connectUsingOAuth(token);
            } else {
                github = GitHub.connectAnonymously();
            }
            GHRepository repo = github.getRepository(repository.toLowerCase().replace("https://github.com/", ""));

            GHContent content = repo.getBranch(branch).getOwner().getFileContent(path, branch);
            Utilities.downloadFileFromGithub(file, content.getDownloadUrl(), token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
