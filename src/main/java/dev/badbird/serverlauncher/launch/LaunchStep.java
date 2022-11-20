package dev.badbird.serverlauncher.launch;

import dev.badbird.serverlauncher.launch.impl.DownloadLaunchStep;
import dev.badbird.serverlauncher.launch.impl.ExecuteLaunchStep;
import dev.badbird.serverlauncher.launch.impl.LoadJarStep;

public abstract class LaunchStep implements Runnable {

    public static enum Type {
        DOWNLOAD(DownloadLaunchStep.class),
        EXECUTE(ExecuteLaunchStep.class),
        LOAD_JAR(LoadJarStep.class);
        Class<? extends LaunchStep> step;

        Type(Class<? extends LaunchStep> step) {
            this.step = step;
        }

        public Class<? extends LaunchStep> getStep() {
            return step;
        }
    }
}
