package org.okunev.chatOverhead.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.okunev.chatOverhead.config.config;
import org.okunev.chatOverhead.display.OverheadDisplayManager;
import org.okunev.chatOverhead.message.MessageService;

public final class PlayerChatListener implements Listener {

    private final JavaPlugin plugin;
    private final config config;
    private final MessageService messages;
    private final OverheadDisplayManager displayManager;

    public PlayerChatListener(JavaPlugin plugin, config config, MessageService messages,
                              OverheadDisplayManager displayManager) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        this.displayManager = displayManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!isAllowed(player)) {
            return;
        }

        String text = event.getMessage();
        if (text.length() > config.getMaxTextLength()) {
            text = text.substring(0, config.getMaxTextLength()) + "...";
        }

        String finalText = text;
        org.bukkit.Bukkit.getScheduler().runTask(
                plugin,
                () -> {
                    if (player.isOnline()) {
                        displayManager.show(player, messages.formatChat(player, finalText));
                    }
                }
        );
    }

    private boolean isAllowed(Player player) {
        if (config.getAllowedPlayers().isEmpty()) {
            return true;
        }
        return config.getAllowedPlayers().stream()
                .anyMatch(name -> name.equalsIgnoreCase(player.getName()));
    }
}
