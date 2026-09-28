package org.okunev.chatOverhead.display;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.okunev.chatOverhead.config.config;
import org.okunev.chatOverhead.display.model.OverheadDisplay;
import org.okunev.chatOverhead.util.ChatTextUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class OverheadDisplayManager {

    private final JavaPlugin plugin;
    private final config config;
    private final Map<UUID, OverheadDisplay> active = new HashMap<>();
    private final Map<UUID, BukkitTask> expiryTasks = new HashMap<>();

    public OverheadDisplayManager(JavaPlugin plugin, config config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void show(Player player, Component message) {
        UUID playerId = player.getUniqueId();
        replaceCurrent(playerId);

        int lineCount = ChatTextUtil.countLines(
                ChatTextUtil.plain(message),
                config.getLineWidth()
        );

        TextDisplay display = createDisplay(player, message);
        if (!player.addPassenger(display)) {
            display.remove();
            plugin.getLogger().warning("Could not attach TextDisplay to " + player.getName());
            return;
        }

        float baseTranslationY = (float) (config.getTextHeight() - player.getHeight());
        active.put(playerId, new OverheadDisplay(playerId, display, lineCount, baseTranslationY));
        scheduleExpiry(playerId, display);
    }

    private TextDisplay createDisplay(Player player, Component message) {
        Location spawnLocation = player.getLocation().clone().add(0, player.getHeight(), 0);
        TextDisplay display = player.getWorld().spawn(spawnLocation, TextDisplay.class);
        display.text(message);
        display.setDefaultBackground(false);
        display.setSeeThrough(false);
        display.setShadowed(true);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setBillboard(TextDisplay.Billboard.CENTER);
        display.setLineWidth(config.getLineWidth());
        display.setTeleportDuration(0);
        display.setInterpolationDuration(0);
        display.setInterpolationDelay(0);
        display.setGravity(false);
        display.setPersistent(false);

        float headOffset = (float) (config.getTextHeight() - player.getHeight());
        display.setTransformation(new Transformation(
                new Vector3f(0, headOffset, 0),
                new Quaternionf(),
                new Vector3f(1, 1, 1),
                new Quaternionf()
        ));
        return display;
    }

    private void replaceCurrent(UUID playerId) {
        BukkitTask expiry = expiryTasks.remove(playerId);
        if (expiry != null) {
            expiry.cancel();
        }

        OverheadDisplay old = active.remove(playerId);
        if (old != null && !old.entity().isDead()) {
            animateOldMessage(old);
        }
    }

    private void animateOldMessage(OverheadDisplay old) {
        TextDisplay display = old.entity();
        if (display.getVehicle() != null) {
            display.leaveVehicle();
        }

        double rise = old.lineCount() * config.getRisePerLine();
        int ticks = config.getRiseAnimationTicks();

        // The display is now independent from the player. Transformation
        // interpolation moves the old message smoothly without per-tick
        // teleports, while the new message remains attached to the player.
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(ticks);
        display.setTransformation(new Transformation(
                new Vector3f(0, old.baseTranslationY() + (float) rise, 0),
                new Quaternionf(),
                new Vector3f(1, 1, 1),
                new Quaternionf()
        ));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!display.isDead()) {
                    display.remove();
                }
            }
        }.runTaskLater(plugin, ticks + 1L);
    }

    private void scheduleExpiry(UUID playerId, TextDisplay display) {
        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                OverheadDisplay current = active.get(playerId);
                if (current == null || current.entity() != display) {
                    cancel();
                    return;
                }

                active.remove(playerId);
                expiryTasks.remove(playerId);
                if (!display.isDead()) {
                    if (display.getVehicle() != null) {
                        display.leaveVehicle();
                    }
                    display.remove();
                }
            }
        }.runTaskLater(plugin, config.getDisplaySeconds() * 20L);

        expiryTasks.put(playerId, task);
    }

    public void shutdown() {
        expiryTasks.values().forEach(BukkitTask::cancel);
        expiryTasks.clear();

        active.values().forEach(entry -> {
            if (!entry.entity().isDead()) {
                entry.entity().remove();
            }
        });
        active.clear();
    }
}
