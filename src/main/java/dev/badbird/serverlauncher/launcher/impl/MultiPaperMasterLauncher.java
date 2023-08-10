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
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class MultiPaperMasterLauncher implements Launcher {
    private static final Logger LOGGER = Logger.getLogger("MultiPaperMasterLauncher");

    private static String jarName = "";

    @SneakyThrows
    @Override
    public void download(LauncherConfig config) {
        String buildVersion = config.getBuildNumber();
        int buildNumber;
        String master_name;
        if (buildVersion.equals("AUTO")) {
            buildNumber = getLatestMasterBuildNumber(config);
        } else buildNumber = Integer.parseInt(buildVersion);

        master_name = getLatestMastername(config, buildNumber);
        System.out.println(master_name);
        //https://multipaper.io/api/v2/projects/multipaper/versions/1.19.1/builds/7/downloads/multipaper-1.19.1-7.jar
        String downloadURL = "https://multipaper.io/api/v2/projects/multipaper/versions/%version%/builds/%build%/downloads/%name%"
                .replace("%version%", config.getVersion())
                .replace("%build%", buildNumber + "")
                .replace("%name%", master_name + "");
        String downloadTarget = config.getDownloadedFileName()
                .replace("%server%", "multipaper")
                .replace("%version%", config.getVersion())
                .replace("%build%", buildNumber + "");
        System.out.println("[MultiPaperMasterLauncher] Downloading server jar build #" + buildNumber + " version " + config.getVersion());
        System.out.println("[MultiPaperMasterLauncher] Downloading to " + downloadTarget + " from " + downloadURL);
        jarName = downloadTarget;
        File file = new File(downloadTarget);
        downloadFile(new URL(downloadURL), file);
    }


    public int getLatestMasterBuildNumber(LauncherConfig cfg) throws Exception {
        String urlString = "https://multipaper.io/api/v2/projects/multipaper/versions/%version%"
                .replace("%version%", cfg.getVersion());
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
        System.out.println("[MultiPaperMasterLauncher] Found latest build for MultiPaper " + cfg.getVersion() + " #" + largest);


        return largest;
    }

    public String getLatestMastername(LauncherConfig cfg, int buildnum) throws Exception {
        String urlString = "https://multipaper.io/api/v2/projects/multipaper/versions/%version%/builds/%build%"
                .replace("%version%", cfg.getVersion())
                .replace("%build%", buildnum + "");
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null)
            sb.append(inputLine).append("\n");
        in.close();

        JsonObject json = JsonParser.parseString(sb.toString()).getAsJsonObject().getAsJsonObject("downloads");
        JsonObject master = json.get("master").getAsJsonObject();
        JsonObject a = JsonParser.parseString(master.toString()).getAsJsonObject();
        String master_name = a.get("name").getAsString();

        return master_name;
    }

    @SneakyThrows
    @Override
    public void launch(LauncherConfig config) {
        if (jarName.isEmpty()) {
            System.out.println("[MultiPaperMasterLauncher] jarName not set, cannot launch");
            return;
        }
        File jarFile = new File(jarName);
        if (!jarFile.exists()) {
            System.out.println("[MultiPaperMasterLauncher] jarFile not found, cannot launch");
            return;
        }
        config.getExtraLaunchProperties().forEach(System::setProperty);
        List<String> args = getLaunchArgs(config);
        JarFile jar = new JarFile(jarFile);
        launchJar(jar, jarFile, args);
    }

    @Override
    public String getFallbackMain() {
        return "io.papermc.paperclip.Main";
    }
}
