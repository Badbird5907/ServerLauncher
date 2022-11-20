package dev.badbird.serverlauncher.util.github;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class GithubFile {
    private String name, path, sha, url, html_url, git_url, download_url, type;

    @SerializedName("_links")
    private JsonObject _links;

    private transient String self, git, html; // Gson should ignore these

    public void populateOther() {
        self = _links.get("self").getAsString();
        git = _links.get("git").getAsString();
        html = _links.get("html").getAsString();
    }

    public void download(String token, File file) {
        System.out.println("Downloading file from Github: " + download_url);
        Utilities.downloadFileFromGithub(file, download_url, token);
    }
}
