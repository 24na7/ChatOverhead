package org.okunev.chatOverhead.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.okunev.chatOverhead.config.config;
import org.okunev.chatOverhead.message.MessageService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChatOverheadCommand implements CommandExecutor, TabCompleter {

    private final config config;
    private final MessageService messages;

    public ChatOverheadCommand(config config, MessageService messages) {
        this.config = config;
        this.messages = messages;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatoverhead.admin")) {
            sender.sendMessage(messages.component("command.no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "add" -> add(sender, args);
            case "remove" -> remove(sender, args);
            case "list" -> list(sender);
            case "reload" -> {
                config.load();
                messages.reload();
                sender.sendMessage(messages.component("command.reloaded"));
            }
            default -> sendHelp(sender);
        }
        return true;
    }

    private void add(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.component("command.usage-add"));
            return;
        }
        String name = args[1];
        if (config.getAllowedPlayers().stream().anyMatch(name::equalsIgnoreCase)) {
            sender.sendMessage(messages.component("command.already-allowed", "%player%", name));
            return;
        }
        config.getAllowedPlayers().add(name.toLowerCase());
        config.saveAllowedPlayers();
        sender.sendMessage(messages.component("command.added", "%player%", name));
    }

    private void remove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messages.component("command.usage-remove"));
            return;
        }
        String name = args[1];
        boolean removed = config.getAllowedPlayers().removeIf(name::equalsIgnoreCase);
        if (!removed) {
            sender.sendMessage(messages.component("command.not-found", "%player%", name));
            return;
        }
        config.saveAllowedPlayers();
        sender.sendMessage(messages.component("command.removed", "%player%", name));
    }

    private void list(CommandSender sender) {
        if (config.getAllowedPlayers().isEmpty()) {
            sender.sendMessage(messages.component("command.list-empty"));
            return;
        }
        sender.sendMessage(messages.component("command.list-header"));
        for (String player : config.getAllowedPlayers()) {
            sender.sendMessage(messages.component("command.list-entry", "%player%", player));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(messages.component("command.help.add"));
        sender.sendMessage(messages.component("command.help.remove"));
        sender.sendMessage(messages.component("command.help.list"));
        sender.sendMessage(messages.component("command.help.reload"));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> values = List.of("add", "remove", "list", "reload");
            List<String> result = new ArrayList<>();
            for (String value : values) {
                if (value.startsWith(args[0].toLowerCase())) {
                    result.add(value);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}
