package com.bettergui.event;

import com.bettergui.BetterGuiMod;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;

/**
 * Rejestruje eventy Fabric API i wysyła powiadomienia do ntfy.
 *
 * Eventy:
 *  - Dołączenie do serwera  → "🟢 Kolega dołączył!"
 *  - Rozłączenie            → "🔴 Kolega wyszedł"
 *  - Śmierć                 → obsługiwana przez DeathScreenMixin
 *  - Osiągnięcia            → obsługiwane przez AdvancementMixin (TODO)
 */
public class GameEventHandler {

    public static void register() {
        // ── Dołączenie do serwera ──────────────────────────────────────────
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String player = getPlayerName(client);
            String server = getServerAddress(client);

            BetterGuiMod.NTFY.sendEvent(
                "🟢 " + player + " dołączył",
                "Gracz dołączył do serwera: " + server
            );
        });

        // ── Rozłączenie ───────────────────────────────────────────────────
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            String player = getPlayerName(client);
            String server = getServerAddress(client);

            BetterGuiMod.NTFY.sendEvent(
                "🔴 " + player + " wyszedł",
                "Gracz opuścił serwer: " + server
            );
        });
    }

    /**
     * Wywoływane przez DeathScreenMixin gdy pojawia się ekran śmierci.
     */
    public static void onPlayerDied(String deathMessage) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String player = getPlayerName(mc);

        BetterGuiMod.NTFY.sendEvent(
            "💀 " + player + " umarł!",
            deathMessage.isEmpty() ? "Zginął w grze" : deathMessage
        );
    }

    /**
     * Wywoływane gdy gracz dostanie osiągnięcie (advancement).
     */
    public static void onAdvancement(String advancementName) {
        MinecraftClient mc = MinecraftClient.getInstance();
        String player = getPlayerName(mc);

        BetterGuiMod.NTFY.sendEvent(
            "🏆 " + player + " zdobył osiągnięcie!",
            advancementName
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String getPlayerName(MinecraftClient mc) {
        if (mc.player != null) return mc.player.getName().getString();
        if (mc.getSession() != null) return mc.getSession().getUsername();
        return "Gracz";
    }

    private static String getServerAddress(MinecraftClient mc) {
        ServerInfo info = mc.getCurrentServerEntry();
        if (info != null) return info.address;
        return "singleplayer";
    }
}
