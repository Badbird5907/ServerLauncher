package dev.badbird.serverlauncher.launcher.impl;

import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.launcher.Launcher;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class PurpurLauncher implements Launcher {
    private static String jarName = "";

    @SneakyThrows
    @Override
    public void download(LauncherConfig config) {
        String buildVersion = config.getBuildNumber();
        int buildNumber;
        if (buildVersion.equals("AUTO")) {
            buildNumber = -1;
        } else buildNumber = Integer.parseInt(buildVersion);
        /*
        String downloadURL = "https://api.purpurmc.org/v2/projects/purpur/versions/%version%/builds/%build%/downloads/paper-%version%-%build%.jar"
                .replace("%version%", config.getVersion())
                .replace("%build%", buildNumber + "");
         */
        String downloadURL;
        if (buildNumber == -1) {
            downloadURL = "https://api.purpurmc.org/v2/purpur/" + config.getVersion() + "/latest/download/";
        } else {
            downloadURL = "https://api.purpurmc.org/v2/purpur/" + config.getVersion() + "/" + buildNumber + "/download/";
        }
        String downloadTarget = config.getDownloadedFileName()
                .replace("%server%", "purpur")
                .replace("%version%", config.getVersion())
                .replace("%build%", buildNumber + "");
        System.out.println("[PurpurLauncher] Downloading server jar build #" + (buildNumber == -1 ? "LATEST" : buildNumber) + " version " + config.getVersion());
        System.out.println("[PurpurLauncher] Downloading to " + downloadTarget + " from " + downloadURL);
        jarName = downloadTarget;
        File file = new File(downloadTarget);
        downloadFile(new URL(downloadURL), file);
    }
    @SneakyThrows
    @Override
    public void launch(LauncherConfig config) {
        if (jarName.isEmpty()) {
            System.out.println("[PurpurLauncher] jarName not set, cannot launch");
            return;
        }
        File jarFile = new File(jarName);
        if (!jarFile.exists()) {
            System.out.println("[PurpurLauncher] jarFile not found, cannot launch");
            return;
        }
        config.getExtraLaunchProperties().forEach(System::setProperty);
        List<String> args = getLaunchArgs(config);
        JarFile jar = new JarFile(jarFile);
        String mainClass;
        if (jar.getManifest() == null) {
            System.err.println("[PurpurLauncher] No manifest found in jar, attempting to launch with pre-set main class");
            mainClass = "io.papermc.paperclip.Main";
        } else {
            mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
        }
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, this.getClass().getClassLoader());
        Class<?> mainClazz;
        try {
            mainClazz = Class.forName(mainClass, true, classLoader);
        } catch (ClassNotFoundException e) {
            System.err.println("[PurpurLauncher] Main class not found in jar, cannot launch.");
            return;
        }
        Method mainMethod = mainClazz.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args.toArray(new String[0]));
    }
}
