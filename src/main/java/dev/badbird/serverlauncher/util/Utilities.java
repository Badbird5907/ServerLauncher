package dev.badbird.serverlauncher.util;

import lombok.SneakyThrows;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;

public class Utilities {
    public static void downloadFile(File file, InputStream is) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        if (file.exists()) file.delete();
        int length;

        byte[] buffer = new byte[1024];
        while ((length = is.read(buffer)) > 0) {
            fos.write(buffer, 0, length);
        }

        fos.close();
        is.close();
        System.out.println("[Downloader] Successfully downloaded file " + file.getName() + " Size: " + Files.size(file.toPath()) + " bytes");
    }

    @SneakyThrows
    public static void downloadFile(File file, String url) {
        downloadFile(file, new URL(url).openStream());
    }

    @SneakyThrows
    public static void writeFile(File file, String content) {
        Files.write(file.toPath(), content.getBytes());
    }

    @SneakyThrows
    public static String readFile(File file) {
        return new String(Files.readAllBytes(file.toPath()));
    }

    @SneakyThrows
    public static long getFileSize(File file) {
        return Files.size(file.toPath());
    }

    @SneakyThrows
    public static void print(File file, String content) {
        PrintStream printStream = new PrintStream(file);
        printStream.println(content);
        printStream.close();
    }
}
