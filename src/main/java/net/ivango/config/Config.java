package net.ivango.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * Stores the simulation configuration.
 * The config is read from the external json file:
 * src/main/resources/configuration.json
 *
 * Created by Ivan Golubev <igolubev@ea.com> on 4/4/16.
 */
public class Config {

    private final static String CONFIG_FILE_NAME = "configuration.json";
    private static Map<String, Integer> CONFIGURATION;

    private static Logger logger = LoggerFactory.getLogger(Config.class);

    public static void init() {
        try {
            ClassLoader classLoader = Config.class.getClassLoader();
            String filePath = classLoader.getResource(CONFIG_FILE_NAME).getFile();
            JsonReader reader = new JsonReader(new FileReader(filePath));
            Type type = new TypeToken<Map<String, Integer>>() {}.getType();
            CONFIGURATION = new Gson().fromJson(reader, type);

        } catch (IOException ie) {
            logger.error("Failed to read the main configuration file", ie);
        }
    }

    public static int get(Properties property) { return CONFIGURATION.get(property.toString()); }
}
