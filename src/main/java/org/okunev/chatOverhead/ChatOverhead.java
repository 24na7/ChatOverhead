package org.okunev.chatOverhead;

import org.bukkit.plugin.java.JavaPlugin;
import org.okunev.chatOverhead.command.ChatOverheadCommand;
import org.okunev.chatOverhead.config.config;
import org.okunev.chatOverhead.display.OverheadDisplayManager;
import org.okunev.chatOverhead.event.PlayerChatListener;
import org.okunev.chatOverhead.message.MessageService;

public final class ChatOverhead extends JavaPlugin {

    private config config;
    private MessageService messageService;
    private OverheadDisplayManager displayManager;

    @Override
    public void onEnable() {
        config = new config(this);
        config.load();

        messageService = new MessageService(this, config);
        displayManager = new OverheadDisplayManager(this, config);

        getServer().getPluginManager().registerEvents(
                new PlayerChatListener(this, config, messageService, displayManager), this
        );

        ChatOverheadCommand command = new ChatOverheadCommand(config, messageService);
        if (getCommand("chatoverhead") != null) {
            getCommand("chatoverhead").setExecutor(command);
            getCommand("chatoverhead").setTabCompleter(command);
        }
        if (getCommand("coh") != null) {
            getCommand("coh").setExecutor(command);
            getCommand("coh").setTabCompleter(command);
        }

        getLogger().info("ChatOverhead enabled. Language: " + config.getLanguageFileName());
    }

    @Override
    public void onDisable() {
        if (displayManager != null) {
            displayManager.shutdown();
        }
    }

    public void reloadPlugin() {
        config.load();
        if (messageService != null) {
            messageService.reload();
        }
    }
}
