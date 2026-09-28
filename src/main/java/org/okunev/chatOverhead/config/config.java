package org.okunev.chatOverhead.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class config {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private String languageFileName;
    private double textHeight;
    private int displaySeconds;
    private int maxTextLength;
    private boolean useMiniMessage;
    private String gradientFormat;
    private List<String> allowedPlayers;
    private int lineWidth;
    private double lineHeight;
    private double risePerLine;
    private int riseAnimationTicks;

    public config(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();

        textHeight = config.getDouble("settings.text-height", 2.4);
        displaySeconds = Math.max(1, config.getInt("settings.display-seconds", 8));
        maxTextLength = Math.max(1, config.getInt("settings.max-text-length", 50));
        useMiniMessage = config.getBoolean("settings.use-minimessage", true);
        gradientFormat = config.getString(
                "settings.gradient-format",
                "<gradient:#FFD700:#FFA500>%message%</gradient>"
        );
        allowedPlayers = new ArrayList<>(config.getStringList("allowed-players"));
        lineWidth = Math.max(1, config.getInt("settings.text-line-width", 200));
        lineHeight = Math.max(0.05, config.getDouble("settings.line-height", 0.25));
        risePerLine = Math.max(0.05, config.getDouble("settings.old-message-rise-per-line", 0.28));
        riseAnimationTicks = Math.max(1, config.getInt("settings.old-message-rise-ticks", 8));

        languageFileName = config.getString("lang", "ru_ru.yml");
        if (languageFileName == null || languageFileName.isBlank()) {
            languageFileName = "ru_ru.yml";
        }
        if (!languageFileName.endsWith(".yml")) {
            languageFileName += ".yml";
        }

        saveDefaultLanguageFiles();
    }

    private void saveDefaultLanguageFiles() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }
        saveResourceIfMissing("lang/ru_ru.yml");
        saveResourceIfMissing("lang/en_us.yml");
    }

    private void saveResourceIfMissing(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    public FileConfiguration getConfig() { return config; }
    public String getLanguageFileName() { return languageFileName; }
    public double getTextHeight() { return textHeight; }
    public int getDisplaySeconds() { return displaySeconds; }
    public int getMaxTextLength() { return maxTextLength; }
    public boolean isUseMiniMessage() { return useMiniMessage; }
    public String getGradientFormat() { return gradientFormat; }
    public List<String> getAllowedPlayers() { return allowedPlayers; }
    public int getLineWidth() { return lineWidth; }
    public double getLineHeight() { return lineHeight; }
    public double getRisePerLine() { return risePerLine; }
    public int getRiseAnimationTicks() { return riseAnimationTicks; }

    public void saveAllowedPlayers() {
        config.set("allowed-players", allowedPlayers);
        plugin.saveConfig();
    }

    public File getLanguageFile() {
        return new File(plugin.getDataFolder(), "lang/" + languageFileName);
    }

    public FileConfiguration loadLanguage() {
        File languageFile = getLanguageFile();
        if (!languageFile.exists()) {
            languageFile = new File(plugin.getDataFolder(), "lang/ru_ru.yml");
        }
        return languageFile.exists()
                ? YamlConfiguration.loadConfiguration(languageFile)
                : new YamlConfiguration();
    }
}
