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

public class ServerLauncher {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    @Getter
    private static String[] args;

    public static void main(String[] args) throws IOException {
        ServerLauncher.args = args;
        LauncherConfig config;
        File configFile = new File("launcher_config.json");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                PrintStream ps = new PrintStream(configFile);
                ps.print(GSON.toJson(new LauncherConfig()));
                ps.close();
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        config = GSON.fromJson(new String(Files.readAllBytes(configFile.toPath())), LauncherConfig.class);
        Launcher launcher = config.getDistro().getLauncher();
        System.out.println("Downloading latest jar");
        launcher.download(config);
    }
}
