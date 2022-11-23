package dev.badbird.serverlauncher.config;

import com.google.gson.JsonObject;
import dev.badbird.serverlauncher.ServerLauncher;
import dev.badbird.serverlauncher.launch.LaunchStep;
import dev.badbird.serverlauncher.util.Utilities;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
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
        if (str.startsWith("%") && str.endsWith("%")) {
            String s = str.substring(1, str.length() - 1);
            if (s.startsWith("env:"))
                return System.getenv(s.substring(4));
            else if (s.startsWith("prop:"))
                return System.getProperty(s.substring(5));
            else {
                return replacements.getOrDefault(s, str);
            }
        }
        return str;
    }

    private static final Pattern PATTERN = Pattern.compile("%.*%");
    public String replaceInText(String str) {
        if (str == null) return null;
        if (PATTERN.matcher(str).find()) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                str = str.replace("%" + entry.getKey() + "%", entry.getValue());
            }
            for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
                str = str.replace("%env:" + entry.getKey() + "%", entry.getValue());
            }
            for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
                str = str.replace("%prop:" + entry.getKey() + "%", entry.getValue().toString());
            }
        }
        return str;
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
        String replaced = replaceInText(content);
        Utilities.writeFile(file, replaced);
        if (content.equals(replaced)) {
            System.out.println("No strings were replaced in file: " + file.getName());
            return;
        }
        System.out.println("Replaced strings in file: " + file.getName());
    }
}
