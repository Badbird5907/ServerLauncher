package dev.badbird.serverlauncher.launch.impl;

import dev.badbird.serverlauncher.config.DownloadConfig;
import dev.badbird.serverlauncher.launch.LaunchStep;

import java.io.File;

public class DownloadLaunchStep extends LaunchStep {
    private String fileName;
    private DownloadConfig source;

    @Override
    public void run() {
        File file = new File(fileName);
        source.download(file);
    }
}
