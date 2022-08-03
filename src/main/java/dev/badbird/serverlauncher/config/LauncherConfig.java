package dev.badbird.serverlauncher.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
@Setter
public class LauncherConfig { // what even is this?
    private ServerDistro distro = ServerDistro.PAPER;
    private String buildNumber = "AUTO";
    private HashMap<String, String> extraLaunchProperties = new HashMap<>();
    private List<String> extraLaunchArgs = new ArrayList<>();
    private String version = "1.19";
    private String downloadedFileName = "server.jar";
}
