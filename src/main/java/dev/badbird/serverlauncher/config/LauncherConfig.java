package dev.badbird.serverlauncher.config;

import dev.badbird.serverlauncher.ServerLauncher;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.*;

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

    public String replace(String str) {
        if (str.startsWith("%") && str.endsWith("%")) {
            String s = str.substring(1, str.length() - 1);
            System.out.println("Deserializing: " + str + ", " + s);
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

    @SneakyThrows
    public static void replaceFields(Object obj, List<Object> visited) {
        if (obj == null) return;
        Field[] fields = obj.getClass().getDeclaredFields();
        visited.add(obj);
        for (Field field : fields) {
            field.setAccessible(true);
            if (visited.contains(field.get(obj))) continue;
            System.out.println("Field: " + field.getName());
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
}
