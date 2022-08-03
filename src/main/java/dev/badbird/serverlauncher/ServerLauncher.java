package dev.badbird.serverlauncher;

import com.google.gson.*;
import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.config.PluginConfig;
import dev.badbird.serverlauncher.launcher.Launcher;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerLauncher {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final File SERVER_LAUNCHER_FOLDER = new File("ServerLauncher");
    private static @Getter List<String> args;
    private static boolean downloadOnly = false;
    private static @Getter LauncherConfig config;

    public static void main(String[] args) throws IOException {
        List<String> arrayList = new ArrayList<>(Arrays.asList(args));
        if (!SERVER_LAUNCHER_FOLDER.exists()) SERVER_LAUNCHER_FOLDER.mkdir();
        while (arrayList.iterator().hasNext()) {
            String arg = arrayList.iterator().next();
            if (arg.equals("--download-only")) {
                downloadOnly = true;
                arrayList.iterator().remove();
            }
        }

        ServerLauncher.args = arrayList;
        File configFile = new File(SERVER_LAUNCHER_FOLDER, "config.json");
        File pluginConfigFile = new File(SERVER_LAUNCHER_FOLDER, "plugin_config.json");
        if (!configFile.exists()) {
            Utilities.print(configFile, GSON.toJson(new LauncherConfig()));
            System.out.println("[Launcher] Created ServerLauncher/config.json, edit it (if needed) and start again.");
        }

        System.setProperty("using.serverlauncher", "true");
        config = GSON.fromJson(new String(Files.readAllBytes(configFile.toPath())), LauncherConfig.class);

        Launcher launcher = config.getDistro().getLauncher();
        if (launcher != null) {
            System.out.println("[Launcher] Downloading latest jar");
            launcher.download(config);
        } else System.out.println("[Launcher] No Launcher!");

        if (pluginConfigFile.exists()) {
            JsonArray jsonArray = JsonParser.parseString(new String(Files.readAllBytes(pluginConfigFile.toPath()))).getAsJsonArray();
            System.out.println("[Launcher] Downloading plugins");
            for (JsonElement je : jsonArray) {
                PluginConfig pluginConfig = GSON.fromJson(je, PluginConfig.class);
                System.out.println("[Launcher] Downloading plugin " + pluginConfig.getFileName());
                pluginConfig.download();
            }

            return;
        }

        if (!downloadOnly && launcher != null) {
            System.out.println("[Launcher] Launching server");
            launcher.launch(config);
            System.out.println("[Launcher] Detected server shutdown, goodnight!");
        }

        pluginConfigFile.createNewFile();
        Utilities.writeFile(pluginConfigFile, "[\n]");
        System.out.println("[Launcher] Created ServerLauncher/plugin_config.json, edit it (if needed) and start again.");
    }
}
