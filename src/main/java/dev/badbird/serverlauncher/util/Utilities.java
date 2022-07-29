package dev.badbird.serverlauncher.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;

public class Utilities {
    public static void downloadFile(File file, InputStream is) throws IOException {
        if (file.exists()) file.delete();
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
        System.out.println("[Downloader] Successfully downloaded file " + file.getName() + " Size: " + Files.size(file.toPath()) + " bytes");
    }

    public static void downloadFile(File file, String url) {
        try {
            downloadFile(file, new URL(url).openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeFile(File file, String content) {
        try {
            Files.write(file.toPath(), content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readFile(File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static long getFileSize(File file) {
        try {
            return Files.size(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
