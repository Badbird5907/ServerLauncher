package dev.badbird.serverlauncher.launcher.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.launcher.Launcher;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class PaperLauncher implements Launcher {

    @SneakyThrows
    @Override
    public void download(LauncherConfig config) {
        URL baseURL = new URL("https://api.papermc.io/");
        String buildVersion = config.getBuildNumber();
        int buildNumber;
        if (buildVersion.equals("AUTO")) {
            buildNumber = getLatestBuildNumber(config);
        }
        else buildNumber = Integer.parseInt(buildVersion);
        String downloadURL = "https://api.papermc.io/v2/projects/paper/versions/%version%/builds/%build%/downloads/paper-%version%-%build%.jar"
                .replace("%version%", config.getVersion())
                .replace("%build%",  buildNumber + "");
        String downloadTarget = config.getDownloadedFileName()
                .replace("%server%", "paper")
                .replace("%version%", config.getVersion())
                .replace("%build%", buildNumber + "");
        System.out.println("Downloading server jar build # " + buildNumber + " version " + config.getVersion());
        System.out.println("Downloading to " + downloadTarget + " from " + downloadURL);
        File file = new File(downloadTarget);
        downloadFile(new URL(downloadURL), file);
    }

    public int getLatestBuildNumber(LauncherConfig cfg) throws Exception {
        String urlString = "https://api.papermc.io/v2/projects/paper/versions/" + cfg.getVersion();
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            sb.append(inputLine).append("\n");
        in.close();
        JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject();
        int largest = 0;
        for (JsonElement build : json.get("builds").getAsJsonArray()) {
            int i = build.getAsInt();
            if (i > largest) largest = i;
        }
        return largest;
    }

    @Override
    public void launch(LauncherConfig config) {
        List<String> properties = getLaunchProperties(config);
        List<String> args = getLaunchArgs(config);

    }
}
