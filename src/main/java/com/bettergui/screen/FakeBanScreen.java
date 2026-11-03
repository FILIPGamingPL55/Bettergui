package com.bettergui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Fake-ban screen imitujący wygląd bana z anarchia.gg / typowego serwera PL.
 *
 * Wygląd:
 * ┌──────────────────────────────────────────────────────────────┐
 * │                   Utracono połączenie                        │
 * │               ◈──────────────────────◈                      │
 * │                                                              │
 * │              Zostałeś zablokowany!                           │
 * │   Twoje konto zostało zablokowane przez [ADMIN]!             │
 * │   Powodem blokady było: [REASON (RED)] ([ADMIN (RED)])       │
 * │              Blokada wygasa [DURATION]                       │
 * │                                                              │
 * │              ◈────── Uwaga! ──────◈                          │
 * │   Pamiętaj, że administracja to nie roboty...                │
 * │   Wejdź na nasz serwer Discord i poczekaj na adm...         │
 * │                                                              │
 * │              Discord – [discord link (CYAN)]                 │
 * └──────────────────────────────────────────────────────────────┘
 *
 * Zamknięcie: Ctrl+Shift+B (ujawnia że to żart) lub ESC po 8s
 */
public class FakeBanScreen extends Screen {

    // Colours
    private static final int COL_BG       = 0xFF000000;
    private static final int COL_WHITE    = 0xFFFFFFFF;
    private static final int COL_GREY     = 0xFFAAAAAA;
    private static final int COL_RED      = 0xFFFF5555;
    private static final int COL_CYAN     = 0xFF55FFFF;
    private static final int COL_GOLD     = 0xFFFFAA00;
    private static final int COL_DIVIDER  = 0xFF555555;

    private final String admin;
    private final String reason;
    private final String duration;
    private final String discordLink;
    private final String serverName;

    /** Czas (ms) od otwarcia ekranu — ESC blokowany przez pierwsze 8s */
    private long openTime;

    /** Ctrl+Shift+B ujawnia że to fake */
    private boolean revealed = false;

    public FakeBanScreen(String admin, String reason, String duration,
                         String discordLink, String serverName) {
        super(Text.literal("Utracono połączenie"));
        this.admin      = admin;
        this.reason     = reason;
        this.duration   = duration;
        this.discordLink = discordLink;
        this.serverName  = serverName;
    }

    @Override
    protected void init() {
        openTime = System.currentTimeMillis();
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int cx = this.width / 2;
        int cy = this.height / 2;

        // Full black background
        ctx.fill(0, 0, this.width, this.height, COL_BG);

        if (revealed) {
            renderReveal(ctx, cx, cy);
            return;
        }

        int y = cy - 100;
        int lineH = 12;

        // "Utracono połączenie"
        drawCentered(ctx, "Utracono połączenie", cx, y, COL_GREY);
        y += lineH + 4;

        // Decorative divider
        drawCentered(ctx, "◈" + "─".repeat(30) + "◈", cx, y, COL_DIVIDER);
        y += lineH + 8;

        // "Zostałeś zablokowany!"
        drawCenteredScaled(ctx, "Zostałeś zablokowany!", cx, y, 1.5f, COL_WHITE);
        y += (int)(lineH * 1.5f) + 8;

        // "Twoje konto zostało zablokowane przez [admin]!"
        drawCentered(ctx, "Twoje konto zostało zablokowane przez " + admin + "!", cx, y, COL_WHITE);
        y += lineH + 4;

        // "Powodem blokady było: [RED reason] ([RED admin])"
        MutableText reasonLine = Text.literal("Powodem blokady było: ")
            .append(Text.literal(reason).formatted(Formatting.RED))
            .append(Text.literal(" (").formatted(Formatting.WHITE))
            .append(Text.literal(admin).formatted(Formatting.RED))
            .append(Text.literal(")").formatted(Formatting.WHITE));
        ctx.drawCenteredTextWithShadow(textRenderer, reasonLine, cx, y, COL_WHITE);
        y += lineH + 4;

        // "Blokada wygasa [duration]"
        drawCentered(ctx, "Blokada wygasa " + duration, cx, y, COL_WHITE);
        y += lineH + 12;

        // "◈── Uwaga! ──◈"
        drawCentered(ctx, "◈─── Uwaga! ───◈", cx, y, COL_DIVIDER);
        y += lineH + 6;

        // Warning text (2 lines, small, grey)
        String warn1 = "Pamiętaj, że administracja to nie roboty – też mamy prawo się pomylić.";
        String warn2 = "Jeżeli masz jakieś pytanie lub Twój ban został nadany bez wskazanego uzasadnienia";
        String warn3 = "wejdź na nasz serwer Discord i poczekaj na administratora, który";
        String warn4 = "nadał Ci blokadę lub na właścicieli.";
        drawCentered(ctx, warn1, cx, y, COL_GREY); y += lineH;
        drawCentered(ctx, warn2, cx, y, COL_GREY); y += lineH;
        drawCentered(ctx, warn3, cx, y, COL_GREY); y += lineH;
        drawCentered(ctx, warn4, cx, y, COL_GREY); y += lineH + 10;

        // Discord link
        MutableText discordText = Text.literal("Discord – ")
            .append(Text.literal(discordLink).formatted(Formatting.AQUA));
        ctx.drawCenteredTextWithShadow(textRenderer, discordText, cx, y, COL_WHITE);

        // Subtle hint after 15s: "naciśnij Ctrl+Shift+B"
        long elapsed = System.currentTimeMillis() - openTime;
        if (elapsed > 15_000) {
            drawCentered(ctx, "[Ctrl+Shift+B]", cx, this.height - 20, 0x33FFFFFF);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderReveal(DrawContext ctx, int cx, int cy) {
        drawCenteredScaled(ctx, "FAKE BAN :)", cx, cy - 20, 2.0f, COL_GOLD);
        drawCentered(ctx, "Naciśnij ESC żeby zamknąć", cx, cy + 20, COL_GREY);
    }

    // ── Input ─────────────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Ctrl + Shift + B → ujawnia fake
        boolean ctrl  = (modifiers & 2) != 0;
        boolean shift = (modifiers & 1) != 0;
        if (ctrl && shift && keyCode == 66) { // B = 66
            revealed = true;
            return true;
        }

        // ESC zablokowany przez pierwsze 8 sekund
        if (keyCode == 256) { // ESC
            long elapsed = System.currentTimeMillis() - openTime;
            if (revealed || elapsed > 8_000) {
                this.client.setScreen(null);
                return true;
            }
            return true; // pochłoń ESC ale nic nie rób
        }

        return false; // pochłoń wszystkie inne klawisze
    }

    @Override
    public boolean shouldCloseOnEsc() { return false; } // obsługujemy ręcznie

    @Override
    public boolean shouldPause() { return true; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void drawCentered(DrawContext ctx, String text, int cx, int y, int color) {
        ctx.drawCenteredTextWithShadow(textRenderer, text, cx, y, color);
    }

    private void drawCenteredScaled(DrawContext ctx, String text, int cx, int y,
                                    float scale, int color) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(cx, y, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        int tw = textRenderer.getWidth(text);
        ctx.drawTextWithShadow(textRenderer, text, -tw / 2, 0, color);
        ctx.getMatrices().pop();
    }
}
