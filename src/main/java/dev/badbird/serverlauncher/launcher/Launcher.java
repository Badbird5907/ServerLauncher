package dev.badbird.serverlauncher.launcher;

import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.config.LauncherConfig;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Launcher {
    void download(LauncherConfig config);

    void launch(LauncherConfig config);

    default List<String> getLaunchProperties(LauncherConfig config) {
        RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
        List<String> jvmArgs = bean.getInputArguments();
        List<String> list = new ArrayList<>(jvmArgs);
        list.addAll(config.getExtraLaunchProperties());
        return list;
    }

    default List<String> getLaunchArgs(LauncherConfig config) {
        List<String> list = new ArrayList<>(Arrays.asList(ServerLauncher.getArgs()));
        list.addAll(config.getExtraLaunchArgs());
        return list;
    }
    default void downloadFile(URL url, File target) throws IOException {
        ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
        FileOutputStream fileOutputStream = new FileOutputStream(target);
        fileOutputStream.getChannel()
                .transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
    }
}
