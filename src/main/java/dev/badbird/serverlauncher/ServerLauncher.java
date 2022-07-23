package dev.badbird.serverlauncher;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.launcher.Launcher;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ServerLauncher {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    private static List<String> args;

    private static boolean downloadOnly = false;

    public static void main(String[] args) throws IOException {
        List<String> a = new ArrayList<>(Arrays.asList(args));
        for (String s : a) {
            if (s.equalsIgnoreCase("--download-only")) {
                downloadOnly = true;
                a.remove(s);
            }
        }
        ServerLauncher.args = a;
        LauncherConfig config;
        File configFile = new File("launcher_config.json");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                PrintStream ps = new PrintStream(configFile);
                ps.print(GSON.toJson(new LauncherConfig()));
                ps.close();
                System.out.println("[Launcher] Created launcher_config.json, edit it (if needed) and start again.");
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        config = GSON.fromJson(new String(Files.readAllBytes(configFile.toPath())), LauncherConfig.class);
        Launcher launcher = config.getDistro().getLauncher();
        System.out.println("[Launcher] Downloading latest jar");
        launcher.download(config);
        if (!downloadOnly) {
            System.out.println("[Launcher] Launching server");
            launcher.launch(config);
        }
    }
}
