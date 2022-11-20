package dev.badbird.serverlauncher;

import com.google.gson.*;
import dev.badbird.serverlauncher.config.DownloadConfig;
import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.config.PluginConfig;
import dev.badbird.serverlauncher.launch.LaunchStep;
import dev.badbird.serverlauncher.launcher.Launcher;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ServerLauncher {
    public static final File SERVER_LAUNCHER_FOLDER = new File("ServerLauncher");
    @Getter
    private static List<String> args;
    private static boolean downloadOnly = false;
    @Getter
    private static LauncherConfig config;

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        List<String> a = new ArrayList<>(Arrays.asList(args));
        Iterator<String> iterator = a.iterator();
        while (iterator.hasNext()) {
            String arg = iterator.next();
            if (arg.equals("--download-only")) {
                downloadOnly = true;
                iterator.remove();
            }
        }
        if (!SERVER_LAUNCHER_FOLDER.exists()) SERVER_LAUNCHER_FOLDER.mkdir();
        ServerLauncher.args = a;
        File configFile = new File(SERVER_LAUNCHER_FOLDER, "config.json");
        File pluginConfigFile = new File(SERVER_LAUNCHER_FOLDER, "plugin_config.json");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                PrintStream ps = new PrintStream(configFile);
                ps.print(GSON.toJson(new LauncherConfig()));
                ps.close();
                System.out.println("[Launcher] Created ServerLauncher/config.json, edit it (if needed) and start again.");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        System.setProperty("using.serverlauncher", "true");
        config = GSON.fromJson(new String(Files.readAllBytes(configFile.toPath())), LauncherConfig.class);
        LauncherConfig.replaceFields(config, new ArrayList<>());

        Launcher launcher = config.getDistro().getLauncher();
        if (launcher != null) {
            System.out.println("[Launcher] Downloading latest jar");
            launcher.download(config);
        } else System.out.println("[Launcher] No Launcher!");

        if (config.getDownloads() != null && !config.getDownloads().isEmpty()) {
            System.out.println("[Launcher] Downloading extra files");
            for (DownloadConfig download : config.getDownloads()) {
                download.download();
            }
        }

        if (pluginConfigFile.exists()) {
            JsonArray jsonArray = JsonParser.parseString(new String(Files.readAllBytes(pluginConfigFile.toPath()))).getAsJsonArray();
            System.out.println("[Launcher] Downloading plugins");
            for (JsonElement je : jsonArray) {
                PluginConfig pluginConfig = GSON.fromJson(je, PluginConfig.class);
                System.out.println("[Launcher] Downloading plugin " + pluginConfig.getFileName());
                pluginConfig.download();
            }
            if (downloadOnly) {
                return;
            }
        } else {
            pluginConfigFile.createNewFile();
            Utilities.writeFile(pluginConfigFile, "[\n]");
            System.out.println("[Launcher] Created ServerLauncher/plugin_config.json, edit it (if needed) and start again.");
        }

        List<LaunchStep> steps = config.getLaunchSteps();
        if (steps != null && !steps.isEmpty()) {
            System.out.println("[Launcher] Running launch steps");
            for (LaunchStep launchStep : steps) {
                launchStep.run();
            }
        }

        if (!downloadOnly && launcher != null) {
            System.out.println("[Launcher] Launching server");
            launcher.launch(config);
        }
    }
}
