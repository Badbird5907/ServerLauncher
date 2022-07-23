package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.ServerLauncher;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Getter
@Setter
public class bStatsConfig {
    private String what_is_bstats = "bStats (https://bStats.org) collects some basic information for plugin authors, like how\n"
            + "many people use their plugin and their total player count. It's recommended to keep bStats\n"
            + "enabled, but if you're not comfortable with this, you can turn this setting off. There is no\n"
            + "performance penalty associated with having metrics enabled, and data sent to bStats is fully\n"
            + "anonymous.";
    private boolean enabled = true, logFailedRequests = false, logSentData = false, logResponseStatusText = false;
    private String serverUuid;

    public bStatsConfig(String serverUuid) {
        this.serverUuid = serverUuid;
    }

    public static File getFile() {
        return new File("serverlauncher_bstats.json");
    }

    public void save() {
        File bStatsJson = getFile();
        if (!bStatsJson.exists()) {
            try {
                bStatsJson.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        String json = ServerLauncher.GSON.toJson(this);
        try {
            Files.write(bStatsJson.toPath(), json.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
