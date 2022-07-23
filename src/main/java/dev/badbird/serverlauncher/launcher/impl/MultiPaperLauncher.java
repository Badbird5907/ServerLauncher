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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class MultiPaperLauncher implements Launcher {
    private static final Logger LOGGER = Logger.getLogger("MultiPaperLauncher");

    private static String jarName = "";

    @SneakyThrows
    @Override
    public void download(LauncherConfig config) {
        String buildVersion = config.getBuildNumber();
        int buildNumber;
        if (buildVersion.equals("AUTO")) {
            buildNumber = getLatestBuildNumber(config);
        } else buildNumber = Integer.parseInt(buildVersion);
        String downloadURL = "https://multipaper.io/api/v2/projects/multipaper/versions/%version%/builds/%build%/downloads/multipaper-%version%-%build%.jar"
                .replace("%version%", config.getVersion())
                .replace("%build%", buildNumber + "");
        String downloadTarget = config.getDownloadedFileName()
                .replace("%server%", "multipaper")
                .replace("%version%", config.getVersion())
                .replace("%build%", buildNumber + "");
        System.out.println("[MultiPaperLauncher] Downloading server jar build #" + buildNumber + " version " + config.getVersion());
        System.out.println("[MultiPaperLauncher] Downloading to " + downloadTarget + " from " + downloadURL);
        jarName = downloadTarget;
        File file = new File(downloadTarget);
        downloadFile(new URL(downloadURL), file);
    }

    public int getLatestBuildNumber(LauncherConfig cfg) throws Exception {
        String urlString = "https://multipaper.io/api/v2/projects/multipaper/versions/" + cfg.getVersion();
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
        System.out.println("[MultiPaperLauncher] Found latest build for MultiPaper " + cfg.getVersion() + " #" + largest);
        return largest;
    }

    @SneakyThrows
    @Override
    public void launch(LauncherConfig config) {
        if (jarName.isEmpty()) {
            System.out.println("[MultiPaperLauncher] jarName not set, cannot launch");
            return;
        }
        File jarFile = new File(jarName);
        if (!jarFile.exists()) {
            System.out.println("[MultiPaperLauncher] jarFile not found, cannot launch");
            return;
        }
        config.getExtraLaunchProperties().forEach(System::setProperty);
        List<String> args = getLaunchArgs(config);
        JarFile jar = new JarFile(jarFile);
        String mainClass;
        if (jar.getManifest() == null) {
            System.err.println("[MultiPaperLauncher] No manifest found in jar, attempting to launch with pre-set main class");
            mainClass = "io.papermc.paperclip.Main";
        } else {
            mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
        }
        URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, this.getClass().getClassLoader());
        Class<?> mainClazz;
        try {
            mainClazz = Class.forName(mainClass, true, classLoader);
        } catch (ClassNotFoundException e) {
            System.err.println("[MultiPaperLauncher] Main class not found in jar, cannot launch.");
            return;
        }
        Method mainMethod = mainClazz.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args.toArray(new String[0]));
    }
}
