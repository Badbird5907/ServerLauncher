package dev.badbird.serverlauncher.launcher.impl;

import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.config.DownloadConfig;
import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.launcher.Launcher;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.JarFile;

public class CustomLauncher implements Launcher {
    private static final File CONFIG_FILE = new File(ServerLauncher.SERVER_LAUNCHER_FOLDER, "custom_launcher.json");
    private DownloadConfig config;

    public CustomLauncher() {
        if (CONFIG_FILE.exists()) {
            String json = Utilities.readFile(CONFIG_FILE);
            config = ServerLauncher.GSON.fromJson(json, DownloadConfig.class);
        } else {
            try {
                CONFIG_FILE.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            Utilities.writeFile(CONFIG_FILE, ServerLauncher.GSON.toJson(new DownloadConfig()));
        }
    }

    @Override
    public void download(LauncherConfig config) {
        if (this.config != null) {
            System.out.println("[Custom Launcher] Downloading custom server - " + config.getDownloadedFileName());
            try {
                this.config.download(new File(config.getDownloadedFileName()));
            } catch (RuntimeException e) {
                System.out.println("[Custom Launcher] Error while downloading custom server - " + e.getLocalizedMessage());
            }
        }
    }

    @SneakyThrows
    @Override
    public void launch(LauncherConfig config) {
        File file = new File(config.getDownloadedFileName());
        if (file.exists()) {
            System.out.println("[Custom Launcher] Launching custom server - " + config.getDownloadedFileName());
            config.getExtraLaunchProperties().forEach(System::setProperty);
            List<String> args = getLaunchArgs(config);
            JarFile jar = new JarFile(file);
            launchJar(jar, file, args);
        } else {
            System.out.println("[Custom Launcher] jarFile not found, cannot launch");
        }
    }
}
