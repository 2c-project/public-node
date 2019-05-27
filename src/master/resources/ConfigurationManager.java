package master.resources;

import master.storage.FilesystemStorage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ConfigurationManager {
    private final static Map<String, String> configs = new HashMap<>();

    private static void updateResources(String newResourceString) {
        if (newResourceString == null) return;
        configs.put(newResourceString.substring(0, newResourceString.indexOf("=")).strip(), newResourceString.substring(newResourceString.indexOf("=") + 1).strip());
    }

    static String getConfig(String name) {
        return configs.get(name);
    }

    public static long getConfigAsLong(String name) {
        return Long.parseLong(configs.get(name));
    }

    public static double getConfigAsDouble(String name) {
        return Double.parseDouble(configs.get(name));
    }

    public static void initConfigs(String externalConfig, String... profiles) {
        initConfig("configs/application.config");
        if (profiles != null)
            for (String profile : profiles) {
                initConfig("configs/application-" + profile + ".config");
            }
        if (externalConfig != null) initExternalConfig(externalConfig);
        FilesystemStorage.loadConfigs();
    }

    private static void initExternalConfig(String resourcePath) {
        InputStream input;
        try {
            input = new FileInputStream(resourcePath);
        } catch (FileNotFoundException e) {
            System.out.println("Can't load configs from external file " + resourcePath);
            return;
        }
        initFromStream(input);
    }

    private static void initConfig(String resourcePath) {
        InputStream input = ConfigurationManager.class.getClassLoader().getResourceAsStream(resourcePath);
        if (input == null) return;
        initFromStream(input);
    }

    private static void initFromStream(InputStream input) {
        try {
            while (input.available() > 0) {
                updateResources(loadNextString(input));
            }
        } catch (IOException e) {
            Logger.getGlobal().warning("Can't finish reading file, error occurred: " + e.getClass() + "[" + e.getMessage() + "]");
        }
    }

    private static String loadNextString(InputStream inputStream) throws IOException {
        int input;
        StringBuilder result = new StringBuilder();
        while ((input = inputStream.read()) != -1) {
            if ((char) input == '\n') return result.toString();
            result.append((char) input);
        }
        return result.toString();
    }

    public static Map<String, String> getAllConfigs() {
        return Map.copyOf(configs);
    }
}