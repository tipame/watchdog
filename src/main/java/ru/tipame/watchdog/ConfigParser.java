package ru.tipame.watchdog;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import ru.tipame.watchdog.dto.Config;

import java.io.File;
import java.net.URL;

/**
 * Created by tipame on 28.10.2017.
 */
public class ConfigParser {

    public static Config parseConfig() {
        try {
            URL configUrl = ConfigParser.class.getClassLoader().getResource("config.json");
            String configStr = FileUtils.readFileToString(new File(configUrl.toURI()));

            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            JsonNode root = mapper.readTree(configStr);
            JsonNode configNode = root.path("config");
            Config config = mapper.readValue(configNode.traverse(), Config.class);
            return config;
        }
        catch (Exception e) {
            throw new RuntimeException("Fail to parse config", e);
        }
    }
}
