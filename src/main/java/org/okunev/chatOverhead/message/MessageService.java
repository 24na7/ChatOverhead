package org.okunev.chatOverhead.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.okunev.chatOverhead.config.config;

public final class MessageService {

    private final config config;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private FileConfiguration messages;

    public MessageService(org.bukkit.plugin.java.JavaPlugin plugin, config config) {
        this.config = config;
        reload();
    }

    public void reload() {
        messages = config.loadLanguage();
    }

    public String get(String path, String... replacements) {
        String value = messages.getString(path, path);
        for (int i = 0; i + 1 < replacements.length; i += 2) {
            value = value.replace(replacements[i], replacements[i + 1]);
        }
        return value;
    }

    public Component component(String path, String... replacements) {
        String value = get(path, replacements);
        if (config.isUseMiniMessage()) {
            return miniMessage.deserialize(value);
        }
        return Component.text(value);
    }

    public Component formatChat(Player player, String message) {
        if (config.isUseMiniMessage() && config.getGradientFormat() != null
                && !config.getGradientFormat().isBlank()) {
            String formatted = config.getGradientFormat()
                    .replace("%message%", message)
                    .replace("%player%", player.getName());
            return miniMessage.deserialize(formatted);
        }
        return Component.text(message).color(NamedTextColor.WHITE);
    }
}
