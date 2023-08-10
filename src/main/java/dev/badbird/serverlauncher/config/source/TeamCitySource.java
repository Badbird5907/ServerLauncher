package dev.badbird.serverlauncher.config.source;

import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.teamcity.rest.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

@Getter
@Setter
public class TeamCitySource implements DownloadSource {
    private String url, username, password, token;

    private String buildConfig = "", artifactName = "", tag, branch, number, revision;

    private boolean latestSuccess = true;

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
        if (useCache) {
            File cacheFile = new File(ServerLauncher.CACHE_FOLDER, "_teamcity_last_build");
            if (cacheFile.exists()) {
                String lastBuild = new String(Files.readAllBytes(cacheFile.toPath()));
                if (lastBuild.trim().equalsIgnoreCase(build.getBuildNumber())) {
                    System.out.println("[TeamCity Downloader] Build #" + build.getBuildNumber() + " is the same as the last build, skipping download");
                    return;
                }
            }
        }
        System.out.println("[TeamCity Downloader] Found build #" + build.getBuildNumber() + " with status " + build.getStatus());
        System.out.println("[TeamCity Downloader] Downloading artifact " + artifactName + " to " + file.getAbsolutePath());
        build.downloadArtifact(artifactName, file);
        System.out.println("[Downloader] Downloaded " + file.getName() + " from TeamCity, size: " + Utilities.getFormattedFileSize(file));
        if (useCache) {
            File cacheFile = new File(ServerLauncher.CACHE_FOLDER, "_teamcity_last_build");
            if (!cacheFile.exists())
                cacheFile.createNewFile();
            FileOutputStream stream = new FileOutputStream(cacheFile);
            stream.write(build.getBuildNumber().getBytes());
            stream.flush();
            stream.close();
        }
    }
}
