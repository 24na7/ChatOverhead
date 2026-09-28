package org.okunev.chatOverhead.display.model;

import org.bukkit.entity.TextDisplay;

import java.util.UUID;

public record OverheadDisplay(UUID playerId, TextDisplay entity, int lineCount, float baseTranslationY) {
}
