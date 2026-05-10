package com.myproxy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {

    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_DIR = "config";
    private static final String CONFIG_FILE = "whitelist.json";

    private final ObjectMapper objectMapper;
    private final File configFile;
    private ProxyConfig config;

    public ConfigManager() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        String workingDir = System.getProperty("user.dir");
        Path configPath = Paths.get(workingDir, CONFIG_DIR);
        this.configFile = configPath.resolve(CONFIG_FILE).toFile();

        try {
            Files.createDirectories(configPath);
        } catch (IOException e) {
            logger.error("Failed to create config directory: {}", configPath, e);
        }
    }

    public void loadConfig() {
        if (configFile.exists()) {
            try {
                config = objectMapper.readValue(configFile, ProxyConfig.class);
                logger.info("Config loaded, port: {}, whitelist size: {}",
                        config.getPort(), config.getAllowedIps().size());
            } catch (IOException e) {
                logger.error("Failed to load config, using defaults", e);
                config = new ProxyConfig();
            }
        } else {
            config = new ProxyConfig();
            saveConfig();
        }
    }

    public void saveConfig() {
        try {
            objectMapper.writeValue(configFile, config);
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    public ProxyConfig getConfig() {
        return config;
    }
}
