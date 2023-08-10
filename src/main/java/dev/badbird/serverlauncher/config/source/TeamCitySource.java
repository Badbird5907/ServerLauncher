package dev.badbird.serverlauncher.config.source;

import com.google.common.reflect.TypeToken;
import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.*;
import org.jetbrains.teamcity.rest.*;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@EqualsAndHashCode
public class TeamCitySource implements DownloadSource {
    private String url, username, password, token;

    private String buildConfig = "", artifactName = "", tag, branch, number, revision;

    private boolean latestSuccess = true;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class LastBuildInfo {
        private TeamCitySource source;
        private String buildNumber;
    }

    private static final Type LAST_BUILD_INFO_LIST = new TypeToken<ArrayList<LastBuildInfo>>(){}.getType();

    @SneakyThrows
    @Override
    public void download(File file) {
        System.out.println("[Downloader] Downloading " + file.getName() + " from TeamCity ");
        TeamCityInstance instance;
        if (token != null) {
            instance = TeamCityInstanceFactory.tokenAuth(url, token);
        } else if (username != null && password != null) {
            instance = TeamCityInstanceFactory.httpAuth(url, username, password);
        } else {
            instance = TeamCityInstanceFactory.guestAuth(url);
        }
        BuildLocator locator = instance.builds()
                .fromConfiguration(new BuildConfigurationId(buildConfig));
        if (tag != null && !tag.isEmpty()) {
            System.out.println(" - With tag " + tag);
            locator = locator.withTag(tag);
        }
        if (branch != null && !branch.isEmpty()) {
            System.out.println(" - With branch " + branch);
            locator = locator.withBranch(branch);
        }
        if (latestSuccess) {
            System.out.println(" - Using latest successful build");
            locator = locator.withStatus(BuildStatus.SUCCESS);
        }
        if (number != null && !number.isEmpty()) {
            System.out.println(" - With build number " + number);
            locator = locator.withNumber(number);
        }
        if (revision != null && !revision.isEmpty()) {
            System.out.println(" - With VCS revision " + revision);
            locator = locator.withVcsRevision(revision);
        }
        Build build = locator.latest();
        if (build == null) {
            throw new RuntimeException("No build found for teamcity build config " + buildConfig + " (plus any other filters)");
        }
        boolean useCache = System.getProperty("dev.badbird.serverlauncher.teamcity.UseCache", "true").equalsIgnoreCase("true");
        List<LastBuildInfo> lastBuildInfos = null;
        if (useCache) { // TODO fix this crap below cause this is for all teamcity builds
            File cacheFile = new File(ServerLauncher.CACHE_FOLDER, "_teamcity_last_build_.json");
            if (cacheFile.exists()) {
                String lastBuildData = new String(Files.readAllBytes(cacheFile.toPath()));
                lastBuildInfos = ServerLauncher.GSON.fromJson(lastBuildData, LAST_BUILD_INFO_LIST);
                if (lastBuildInfos != null && !lastBuildInfos.isEmpty()) {
                    LastBuildInfo lastBuildInfo = lastBuildInfos.stream().filter(info -> info.getSource().equals(this)).findFirst().orElse(null);
                    if (lastBuildInfo != null) {
                        if (Objects.equals(lastBuildInfo.getBuildNumber(), build.getBuildNumber())) {
                            System.out.println("[TeamCity Downloader] Build #" + build.getBuildNumber() + " is the same as the last build, skipping download");
                            return;
                        }
                    }
                }
            }
        }
        System.out.println("[TeamCity Downloader] Found build #" + build.getBuildNumber() + " with status " + build.getStatus());
        System.out.println("[TeamCity Downloader] Downloading artifact " + artifactName + " to " + file.getAbsolutePath());
        build.downloadArtifact(artifactName, file);
        System.out.println("[Downloader] Downloaded " + file.getName() + " from TeamCity, size: " + Utilities.getFormattedFileSize(file));
        if (useCache) {
            File cacheFile = new File(ServerLauncher.CACHE_FOLDER, "_teamcity_last_build_.json");
            if (!cacheFile.exists()) cacheFile.createNewFile();
            if (lastBuildInfos == null) lastBuildInfos = new ArrayList<>();
            lastBuildInfos.removeIf(info -> info.getSource().equals(this));
            lastBuildInfos.add(new LastBuildInfo(this, build.getBuildNumber()));
            FileOutputStream stream = new FileOutputStream(cacheFile);
            stream.write(ServerLauncher.GSON.toJson(lastBuildInfos).getBytes());
            stream.flush();
            stream.close();
        }
    }
}
