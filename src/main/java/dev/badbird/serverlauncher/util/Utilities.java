package dev.badbird.serverlauncher.util;

import dev.badbird.serverlauncher.ServerLauncher;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utilities {
    public static void downloadFile(File file, InputStream is) throws IOException {
        if (file.exists()) file.delete();
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = is.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
        }
        fos.close();
        is.close();
        System.out.println("[Downloader] Successfully downloaded file " + file.getName() + " Size: " + Files.size(file.toPath()) + " bytes");
        ServerLauncher.getConfig().replaceStringsInFile(file);
    }

    public static void downloadFile(File file, String url) {
        try {
            downloadFile(file, new URL(url).openStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static void downloadFileFromGithub(File file, String urlStr, String token) {
        // Authorization: token <token>
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Authorization", "token " + token);
        connection.setRequestMethod("GET");
        connection.connect();
        downloadFile(file, connection.getInputStream());
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

    public static String getFormattedFileSize(File file) {
        long size = getFileSize(file);
        if (size > 1024 * 1024 * 1024) {
            return String.format("%.2f GB", size / (1024.0 * 1024.0 * 1024.0));
        } else if (size > 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024.0));
        } else if (size > 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else {
            return size + " bytes";
        }
    }

    public static final List<String> WHITELISTED_FILE_SUFFIXES = new ArrayList<>(Arrays.asList(".json", ".txt", ".yml", ".properties"));

    public static boolean isWhitelisted(File file) {
        String name = file.getName();
        for (String suffix : WHITELISTED_FILE_SUFFIXES) {
            if (name.endsWith(suffix)) return true;
        }
        return false;
    }

    public static String readStream(InputStream inputStream) {
        try {
            StringBuilder sb = new StringBuilder();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                sb.append(new String(buffer, 0, len));
            }
            inputStream.close();
            return sb.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
