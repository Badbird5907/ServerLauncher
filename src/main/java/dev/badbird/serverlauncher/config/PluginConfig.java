package dev.badbird.serverlauncher.config;

import lombok.Getter;

import java.io.File;

@Getter
public class PluginConfig {

    private String fileName; // another unused variable!!
    private final DownloadConfig source = new DownloadConfig();

    public void download() {
        File file = new File("plugins", fileName);
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        source.download(file);
    }
}
