package dev.badbird.serverlauncher.config;

import lombok.Getter;

import java.io.File;

@Getter
public class PluginConfig {
    private String fileName;

    private DownloadConfig source = new DownloadConfig();

    public void download() {
        File file = new File("plugins", fileName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (source == null) throw new RuntimeException("Source is not set!");
        source.download(file);
    }
}
