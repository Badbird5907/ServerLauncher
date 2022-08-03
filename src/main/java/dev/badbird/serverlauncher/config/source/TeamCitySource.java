package dev.badbird.serverlauncher.config.source;

import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.teamcity.rest.*;

import java.io.File;

@Getter
@Setter
public class TeamCitySource implements DownloadSource {
    private String url, username, password, token;

    private String buildConfig = "", artifactName = "", tag, branch, number, revision;

    private boolean latestSuccess = true;

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

        BuildLocator locator = instance.builds().fromConfiguration(new BuildConfigurationId(buildConfig));
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
            throw new RuntimeException("No build found for teamcity build config " + buildConfig);
        }

        System.out.println("[TeamCity Downloader] Found build #" + build.getBuildNumber() + " with status " + build.getStatus());
        build.downloadArtifact(artifactName, file);
        System.out.println("[Downloader] Downloaded " + file.getName() + " from TeamCity, size: " + Utilities.getFileSize(file));
    }
}
