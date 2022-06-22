package dev.badbird.serverlauncher.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LauncherConfig {
    private ServerDistro distro = ServerDistro.PAPER;
    private String buildNumber = "AUTO";
    private List<String> extraLaunchProperties = new ArrayList<>();
    private List<String> extraLaunchArgs = new ArrayList<>();
    private String version = "1.19";
    private String downloadedFileName = "server.jar";
}
