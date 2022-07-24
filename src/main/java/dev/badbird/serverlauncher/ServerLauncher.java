package dev.badbird.serverlauncher;

import com.google.gson.*;
import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.config.PluginConfig;
import dev.badbird.serverlauncher.launcher.Launcher;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerLauncher {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String VERSION = "1.0.1";
    public static final File SERVER_LAUNCHER_FOLDER = new File("ServerLauncher");
    @Getter
    private static List<String> args;
    private static boolean downloadOnly = false;
    @Getter
    private static LauncherConfig config;

    public static void main(String[] args) throws IOException {
        List<String> a = new ArrayList<>(Arrays.asList(args));
        for (String s : a) {
            if (s.equalsIgnoreCase("--download-only")) {
                downloadOnly = true;
                a.remove(s);
            }
        }
        if (!SERVER_LAUNCHER_FOLDER.exists()) SERVER_LAUNCHER_FOLDER.mkdir();
        ServerLauncher.args = a;
        File configFile = new File(SERVER_LAUNCHER_FOLDER, "launcher_config.json");
        if (!configFile.exists()) {
            File oldConfig = new File("launcher_config.json");
            if (oldConfig.exists()) {
                Files.move(oldConfig.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[Launcher] Moved old launcher_config.json to folder ServerLauncher");
            } else {
                try {
                    configFile.createNewFile();
                    PrintStream ps = new PrintStream(configFile);
                    ps.print(GSON.toJson(new LauncherConfig()));
                    ps.close();
                    System.out.println("[Launcher] Created ServerLauncher/launcher_config.json, edit it (if needed) and start again.");
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
        config = GSON.fromJson(new String(Files.readAllBytes(configFile.toPath())), LauncherConfig.class);

        Launcher launcher = config.getDistro().getLauncher();
        System.out.println("[Launcher] Downloading latest jar");
        launcher.download(config);

        File pluginConfigFile = new File(SERVER_LAUNCHER_FOLDER, "plugin_config.json");
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
        }

        if (!downloadOnly) {
            System.out.println("[Launcher] Launching server");
            launcher.launch(config);
            System.out.println("[Launcher] Detected server shutdown, goodnight!");
        }
    }
}
