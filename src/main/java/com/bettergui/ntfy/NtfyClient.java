package com.bettergui.ntfy;

import com.bettergui.BetterGuiMod;
import com.bettergui.screen.FakeBanScreen;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

/**
 * ntfy.sh HTTP client.
 *
 * ── Wysyłanie (POST) ────────────────────────────────────────────────────────
 *   sendEvent("💀 Kolega umarł", "Spadł z klifu")
 *   → pojawia się powiadomienie na Twoim telefonie
 *
 * ── Odbieranie (SSE) ────────────────────────────────────────────────────────
 *   Nasłuchuje na topic w tle.
 *   Gdy wyślesz z telefonu/PC:
 *     curl -H "Title: FAKEBAN" -d "xHiroo_|Wykryto cheaty|7d" ntfy.sh/<topic>
 *   → kolega widzi fake-ban screen!
 *
 * ── Komendy (title wiadomości) ──────────────────────────────────────────────
 *   FAKEBAN   → message: "Admin|Powód|Czas"  (np. "xHiroo_|Cheat|7d")
 *   UNFAKEBAN → zamknij fake-ban screen (jeśli nadal widoczny)
 *   CHAT      → message: tekst który pojawi się w chacie (jako wiadomość)
 */
public class NtfyClient {

    private final NtfyConfig cfg;
    private volatile boolean running = false;
    private Thread sseThread;

    public NtfyClient(NtfyConfig cfg) {
        this.cfg = cfg;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    public void start() {
        running = true;
        sseThread = new Thread(this::sseLoop, "bettergui-ntfy");
        sseThread.setDaemon(true);
        sseThread.start();
        BetterGuiMod.LOGGER.info("[BetterGUI] ntfy listener started → {}/{}", cfg.server, cfg.topic);
    }

    public void stop() {
        running = false;
        if (sseThread != null) sseThread.interrupt();
    }

    // ── SSE listener ─────────────────────────────────────────────────────────

    private void sseLoop() {
        while (running) {
            try {
                connectSSE();
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                BetterGuiMod.LOGGER.debug("[BetterGUI] ntfy SSE disconnected ({}), retry in 30s", e.getMessage());
                try { Thread.sleep(30_000); } catch (InterruptedException ie) { break; }
            }
        }
    }

    private void connectSSE() throws Exception {
        URL url = new URL(cfg.server + "/" + cfg.topic + "/sse");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(15_000);
        conn.setReadTimeout(360_000); // 6 min — ntfy keeps alive ~10 min

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (running && (line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    processEvent(line.substring(5).trim());
                }
            }
        } finally {
            conn.disconnect();
        }
    }

    private void processEvent(String json) {
        if (json.isEmpty() || json.equals("{}")) return;

        String title   = extractJson(json, "title");
        String message = extractJson(json, "message");
        String event   = extractJson(json, "event");

        // ntfy sends a "open" keepalive event — ignore
        if ("open".equals(event) || "keepalive".equals(event)) return;

        BetterGuiMod.LOGGER.debug("[BetterGUI] ntfy event: title={} msg={}", title, message);

        switch (title.toUpperCase().trim()) {
            case "FAKEBAN" -> handleFakeBan(message);
            case "UNFAKEBAN" -> handleUnfakeBan();
            // extensible: add more commands here
        }
    }

    // ── Command handlers ─────────────────────────────────────────────────────

    /**
     * Komenda: curl -H "Title: FAKEBAN" -d "Admin|Powód|7d" ntfy.sh/<topic>
     */
    private void handleFakeBan(String message) {
        String[] parts = message.split("\\|", 3);
        String admin    = parts.length > 0 ? parts[0].trim() : "Admin";
        String reason   = parts.length > 1 ? parts[1].trim() : "Naruszenie regulaminu";
        String duration = parts.length > 2 ? parts[2].trim() : "7d";

        BetterGuiMod.LOGGER.info("[BetterGUI] FAKEBAN triggered: admin={} reason={} dur={}", admin, reason, duration);

        // Musi działać na głównym wątku MC
        MinecraftClient.getInstance().execute(() ->
            MinecraftClient.getInstance().setScreen(
                new FakeBanScreen(admin, reason, duration, cfg.discordLink, cfg.serverName))
        );
    }

    private void handleUnfakeBan() {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.execute(() -> {
            if (mc.currentScreen instanceof FakeBanScreen) {
                mc.setScreen(null);
            }
        });
    }

    // ── Send event ───────────────────────────────────────────────────────────

    /**
     * Wysyła powiadomienie na ntfy (np. śmierć, dołączenie do serwera).
     * Wykonuje się asynchronicznie — nie blokuje gry.
     *
     * @param title   tytuł powiadomienia (emoji + krótki opis)
     * @param message treść (np. "Spadł z klifu w xxx")
     */
    public void sendEvent(String title, String message) {
        Thread t = new Thread(() -> {
            try {
                URL url = new URL(cfg.server + "/" + cfg.topic);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Title", title);
                conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                conn.setConnectTimeout(8_000);
                conn.setReadTimeout(8_000);
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    os.write(message.getBytes(StandardCharsets.UTF_8));
                }

                int code = conn.getResponseCode();
                if (code != 200) {
                    BetterGuiMod.LOGGER.warn("[BetterGUI] ntfy POST returned {}", code);
                }
                conn.disconnect();
            } catch (Exception e) {
                BetterGuiMod.LOGGER.debug("[BetterGUI] ntfy send failed: {}", e.getMessage());
            }
        }, "bettergui-ntfy-send");
        t.setDaemon(true);
        t.start();
    }

    // ── Mini JSON parser (bez zewnętrznych deps) ─────────────────────────────

    private String extractJson(String json, String field) {
        String key = "\"" + field + "\":\"";
        int s = json.indexOf(key);
        if (s < 0) return "";
        s += key.length();
        StringBuilder sb = new StringBuilder();
        for (int i = s; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') break;
            if (c == '\\' && i + 1 < json.length()) {
                i++;
                switch (json.charAt(i)) {
                    case 'n' -> sb.append('\n');
                    case 't' -> sb.append('\t');
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    default -> sb.append(json.charAt(i));
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
