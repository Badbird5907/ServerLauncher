package dev.badbird.serverlauncher.config.source;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.util.Utilities;
import dev.badbird.serverlauncher.util.github.GithubFile;
import lombok.SneakyThrows;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

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
            try {
                GHContent content = repo.getFileContent(path, branch);
                download(content, token, file);
            } catch (IOException e) {
                System.err.println("[Downloader] Error was thrown by library while downloading from Github, trying to download manually: ");
                System.err.println(e.getMessage());
                // Send a request to the API to get the list of files in the directory because the api lib is broken
                String apiURL = "https://api.github.com/repos/" + repository + "/contents/" + path + "/?ref=" + branch;
                System.out.println("Downloading directory from Github: " + path);
                URL url = new URL(apiURL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Authorization", "token " + token);
                connection.setRequestMethod("GET");
                connection.connect();
                String json = Utilities.readStream(connection.getInputStream());
                JsonObject[] files = ServerLauncher.GSON.fromJson(json, JsonObject[].class);
                for (JsonObject fileObject : files) {
                    String name = fileObject.get("name").getAsString();
                    String downloadURL = fileObject.get("download_url").getAsString();
                    System.out.println("Downloading file from Github: " + downloadURL);
                    Utilities.downloadFileFromGithub(new File(file, name), downloadURL, token);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    private void download(GHContent content, String token, File file) {
        System.out.println("Downloading file from Github: " + content.getDownloadUrl());
        if (content.isDirectory()) {
            /*
            if (System.getProperty("dev.badbird.serverlauncher.dir", "false").equalsIgnoreCase("false")) {
                System.err.println("Downloading directories from github is currently broken!");
                System.err.println("Please set the system property 'dev.badbird.serverlauncher.dir' to 'true' to enable this feature.");
                return;
            }
             */
            String name = content.getName();
            System.out.println("File is a directory: " + name);
            File dir = new File(file, name);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            /*
            for (GHContent ghContent : content.listDirectoryContent()) {
                download(ghContent, token, new File(file, ghContent.getName()));
            }
             */
            //https://api.github.com/repos/AbsenceMC/Config/contents/plugins/AquaCore/?ref=main
            String apiURL = "https://api.github.com/repos/" + repository + "/contents/" + path + "/?ref=" + branch;
            System.out.println("Downloading directory from Github: " + path);
            URL url = new URL(apiURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("Authorization", "token " + token);
            connection.setRequestMethod("GET");
            connection.connect();
            String json = Utilities.readStream(connection.getInputStream());
            GithubFile[] files = ServerLauncher.GSON.fromJson(json, GithubFile[].class);
            for (GithubFile githubFile : files) {
                githubFile.populateOther();
                githubFile.download(token, new File(dir, githubFile.getName()));
            }
        } else {
            Utilities.downloadFileFromGithub(file, content.getDownloadUrl(), token);
        }
    }
}
