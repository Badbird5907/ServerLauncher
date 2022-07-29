package dev.badbird.serverlauncher.config.source;

import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
public class URLSource implements DownloadSource {
    private String url;

    @Override
    public void download(File file) {
        Utilities.downloadFile(file, url);
    }
}
