package dev.badbird.serverlauncher.launcher;

import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.config.LauncherConfig;
import dev.badbird.serverlauncher.util.JarLoader;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

public interface Launcher {
    void download(LauncherConfig config);

    void launch(LauncherConfig config);

    default List<String> getLaunchArgs(LauncherConfig config) {
        List<String> list = new ArrayList<>(ServerLauncher.getArgs());
        list.addAll(config.getExtraLaunchArgs());
        return list;
    }

    default void downloadFile(URL url, File target) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(target);
        fileOutputStream.getChannel()
                .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }

    default String getFallbackMain() {
        return null;
    }

    @SneakyThrows
    default void launchJar(JarFile jar, File file, List<String> args) {
        String mainClass;
        if (jar.getManifest() == null) {
            System.err.println("[Launcher] No manifest found in jar, attempting to launch with pre-set main class");
            mainClass = getFallbackMain();
            if (mainClass == null) {
                System.err.println("[Launcher] No fallback main class found, cannot launch");
                return;
            }
        } else {
            mainClass = jar.getManifest().getMainAttributes().getValue("Main-Class");
        }
        URLClassLoader classLoader = null;
        if (System.setProperty("dev.badbird.serverlauncher.UseNewClassLoader", "false").equalsIgnoreCase("true")) {
            classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
        } else {
            JarLoader.addToClassPath(file); // We want to load the jar with the system class loader because some servers assume they're running with the system class loader.
            classLoader = (URLClassLoader) this.getClass().getClassLoader();
        }
        Class<?> mainClazz;
        try {
            mainClazz = Class.forName(mainClass, true, classLoader);
        } catch (ClassNotFoundException e) {
            System.err.println("[Launcher] Main class not found in jar, cannot launch.");
            return;
        }
        Method mainMethod = mainClazz.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args.toArray(new String[0]));
    }
}
