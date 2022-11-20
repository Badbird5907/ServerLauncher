package dev.badbird.serverlauncher.launch.impl;

import dev.badbird.serverlauncher.launch.LaunchStep;
import dev.badbird.serverlauncher.util.JarLoader;

import java.io.File;
import java.util.Objects;

public class LoadJarStep extends LaunchStep {
    private String file; // Can be file or directory
    private String[] classesToCall;

    @Override
    public void run() {
        try {
            System.out.println("[Launch Step (jar load)] Loading jar: " + file);
            File jar = new File(file);
            if (!jar.exists()) {
                System.err.println("[Launch Step (jar load)] File does not exist: " + file);
                return;
            }
            if (jar.isDirectory()) {
                for (File file : Objects.requireNonNull(jar.listFiles())) {
                    if (file.getName().endsWith(".jar")) {
                        System.out.println("[Launch Step (jar load)] Loading jar: " + file.getName());
                        JarLoader.addToClassPath(file);
                    }
                }
            } else {
                JarLoader.addToClassPath(jar);
            }
            if (classesToCall != null) {
                for (String className : classesToCall) {
                    System.out.println("[Launch Step (jar load)] Calling class: " + className);
                    Class.forName(className);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
