package dev.badbird.serverlauncher.launch.impl;

import dev.badbird.serverlauncher.launch.LaunchStep;

public class ExecuteLaunchStep extends LaunchStep {
    private String command;

    @Override
    public void run() {
        try {
            System.out.println("[Launch Step] Running command: " + command);
            ProcessBuilder builder = new ProcessBuilder(command.split(" "))
                    .inheritIO();
            Process process = builder.start();
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
