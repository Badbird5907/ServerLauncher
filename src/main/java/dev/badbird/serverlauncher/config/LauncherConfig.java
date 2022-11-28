package dev.badbird.serverlauncher.config;

import com.google.gson.JsonObject;
import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.launch.LaunchStep;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class LauncherConfig {
    private ServerDistro distro = ServerDistro.PAPER;
    private String buildNumber = "AUTO";
    private HashMap<String, String> extraLaunchProperties = new HashMap<>();
    private List<String> extraLaunchArgs = new ArrayList<>();
    private String version = "1.19.2";
    private String downloadedFileName = "server.jar";
    private Map<String, String> replacements = new HashMap<>();
    private List<DownloadConfig> downloads = new ArrayList<>();
    private List<JsonObject> launchSteps = new ArrayList<>();
    private List<String> whitelistedFileSuffixes = null;
    private boolean replaceStringsAfterDownload = true;

    public List<LaunchStep> getLaunchSteps() {
        List<LaunchStep> steps = new ArrayList<>();
        for (JsonObject object : launchSteps) {
            LaunchStep.Type type = LaunchStep.Type.valueOf(object.get("type").getAsString().toUpperCase());
            LaunchStep step = ServerLauncher.GSON.fromJson(object, type.getStep());
            steps.add(step);
        }
        LauncherConfig.replaceFields(steps, new ArrayList<>());
        return steps;
    }

    public String replace(String str) {
        /*
        if (str.startsWith("%") && str.endsWith("%")) {
            String s = str.substring(1, str.length() - 1);
            // to get env or prop, use %env:hello% or %prop:hello%, defaults will also work, so %env:hello:abc% will return abc if hello is not set, and same for prop
            String[] split = s.split(":");
            for (int i = 0; i < split.length; i++) {
                try {
                    split[i] = URLDecoder.decode(split[i], "UTF-8"); // decode the string, so we can use : in the string (%3A)
                } catch (UnsupportedEncodingException e) {
                    System.out.println("Failed to decode string: " + split[i]);
                }
            }
            if (split.length == 1) {
                return replacements.getOrDefault(s, str);
            } else if (split.length == 2) {
                if (split[0].equals("env")) {
                    return System.getenv(split[1]);
                } else if (split[0].equals("prop")) {
                    return System.getProperty(split[1]);
                } else {
                    return replacements.getOrDefault(s, str);
                }
            } else if (split.length == 3) {
                if (split[0].equals("env")) {
                    return System.getenv(split[1]);
                } else if (split[0].equals("prop")) {
                    return System.getProperty(split[1]);
                } else {
                    return replacements.getOrDefault(s, str);
                }
            } else {
                return str;
            }
        }
        return str;
         */
        // We have to replace inside strings, so we need to use regex
        Pattern pattern = Pattern.compile("%(.*?)%");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()) {
            String s = matcher.group(1);
            String[] split = s.split(":");
            for (int i = 0; i < split.length; i++) {
                try {
                    split[i] = URLDecoder.decode(split[i], "UTF-8"); // decode the string, so we can use : in the string (%3A)
                } catch (UnsupportedEncodingException e) {
                    System.err.println("Failed to decode string: " + split[i]);
                }
            }
            if (split.length == 1) {
                str = str.replace(matcher.group(), replacements.getOrDefault(s, matcher.group()));
            } else if (split.length == 2) {
                if (split[0].equals("env")) {
                    str = str.replace(matcher.group(), System.getenv(split[1]));
                } else if (split[0].equals("prop")) {
                    str = str.replace(matcher.group(), System.getProperty(split[1]));
                } else {
                    str = str.replace(matcher.group(), replacements.getOrDefault(s, matcher.group()));
                }
            } else if (split.length == 3) {
                if (split[0].equals("env")) {
                    String env = System.getenv(split[1]);
                    if (env == null) env = split[2];
                    str = str.replace(matcher.group(), env);
                } else if (split[0].equals("prop")) {
                    str = str.replace(matcher.group(), System.getProperty(split[1], split[2]));
                } else {
                    str = str.replace(matcher.group(), replacements.getOrDefault(s, matcher.group()));
                }
            }
        }
        return str;
    }

    public static void main(String[] args) {
        LauncherConfig dummy = new LauncherConfig();
        dummy.replacements.put("hello", "world");
        dummy.replacements.put("test", "Hi!");
        String s1 = "random text 123 %hello% hello world!";
        String s2 = "eee %prop:test:123% cool";
        System.out.println(dummy.replace(s1));
        System.out.println(dummy.replace(s2));
    }

    @SneakyThrows
    public static void replaceFields(Object obj, List<Object> visited) {
        if (obj == null) return;
        Field[] fields = obj.getClass().getDeclaredFields();
        visited.add(obj);
        for (Field field : fields) {
            field.setAccessible(true);
            if (visited.contains(field.get(obj))) continue;
            Object o = field.get(obj);
            if (o instanceof String) {
                try {
                    String str = (String) field.get(obj);
                    if (str != null) {
                        field.set(obj, ServerLauncher.getConfig().replace(str));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            } else if (o instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) o;
                for (Object o1 : collection) {
                    replaceFields(o1, visited);
                }
            } else if (o instanceof Map<?, ?>) {
                Map<?, ?> map = (Map<?, ?>) o;
                for (Object o1 : map.keySet()) {
                    replaceFields(o1, visited);
                    replaceFields(map.get(o1), visited);
                }
            } else {
                replaceFields(field.get(obj), visited);
            }
        }
    }

    public void replaceStringsInFile(File file) {
        System.out.println("Replacing strings in file: " + file.getName());
        if (replaceStringsAfterDownload && !Utilities.isWhitelisted(file) || !file.exists()) {
            System.out.println("File is not a plain text file, or doesn't exist! skipping string replacement.");
            return;
        }
        String content = Utilities.readFile(file);
        String replaced = replace(content);
        Utilities.writeFile(file, replaced);
        if (content.equals(replaced)) {
            System.out.println("No strings were replaced in file: " + file.getName());
            return;
        }
        System.out.println("Replaced strings in file: " + file.getName());
    }
}
