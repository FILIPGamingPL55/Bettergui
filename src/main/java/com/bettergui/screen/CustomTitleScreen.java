package com.bettergui.screen;

import com.bettergui.screen.widget.FeatherButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/**
 * Feather Client-inspired title screen.
 *
 *  ┌──────────────────────────────────────────────────────┐
 *  │  [profile top-right]                                  │
 *  │                                                       │
 *  │              BETTER  GUI          ← scaled logo       │
 *  │           Minecraft 1.20.6                            │
 *  │                                                       │
 *  │           ┌─────────────────┐                         │
 *  │           │  Singleplayer   │                         │
 *  │           │  Multiplayer    │                         │
 *  │           │  Cosmetics      │                         │
 *  │           │  Options        │                         │
 *  │           │  Quit Game  ███ │  ← danger red           │
 *  │           └─────────────────┘                         │
 *  │  BetterGUI 1.0.0 · MC 1.20.6              bottom     │
 *  └──────────────────────────────────────────────────────┘
 */
public class CustomTitleScreen extends Screen {

    // Layout
    private static final int BTN_W    = 260;
    private static final int BTN_H    = 40;
    private static final int BTN_GAP  = 8;

    // Colours
    private static final int COL_BG       = 0xF2101010;
    private static final int COL_OVERLAY  = 0x88000000;
    private static final int COL_WHITE    = 0xFFFFFFFF;
    private static final int COL_GREY     = 0xFFAAAAAA;
    private static final int COL_ACCENT   = 0xFFD04040;   // red like Feather Store btn

    private int cx; // screen centre X

    public CustomTitleScreen() {
        super(Text.literal("BetterGUI"));
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        cx = this.width / 2;

        int bx    = cx - BTN_W / 2;
        int baseY = this.height / 2 - 50;

        // Singleplayer
        addBtn(bx, baseY, "Singleplayer",
                btn -> client.setScreen(new SelectWorldScreen(this)),
                FeatherButton.Style.DEFAULT);

        // Multiplayer
        addBtn(bx, baseY + step(1), "Multiplayer",
                btn -> client.setScreen(new MultiplayerScreen(this)),
                FeatherButton.Style.DEFAULT);

        // Cosmetics → Cape selection
        addBtn(bx, baseY + step(2), "Cosmetics",
                btn -> client.setScreen(new CapeSelectionScreen(this)),
                FeatherButton.Style.PRIMARY);

        // Options
        addBtn(bx, baseY + step(3), "Options",
                btn -> client.setScreen(new GameOptionsScreen(this, client.options)),
                FeatherButton.Style.DEFAULT);

        // Quit
        addBtn(bx, baseY + step(4), "Quit Game",
                btn -> client.scheduleStop(),
                FeatherButton.Style.DANGER);
    }

    private void addBtn(int x, int y, String label,
                        ButtonWidget.PressAction action,
                        FeatherButton.Style style) {
        addDrawableChild(
            FeatherButton.builder(Text.literal(label), action)
                .dimensions(x, y, BTN_W, BTN_H)
                .style(style)
                .build()
        );
    }

    private int step(int i) { return i * (BTN_H + BTN_GAP); }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Full-screen dark background
        ctx.fill(0, 0, this.width, this.height, COL_BG);

        // Subtle top vignette
        for (int y = 0; y < 80; y++) {
            int a = (int)(50 * (1f - y / 80f));
            ctx.fill(0, y, this.width, y + 1, (a << 24));
        }

        // Logo
        renderLogo(ctx);

        // Version footer
        ctx.drawText(textRenderer,
                "BetterGUI 1.0.0  ·  MC 1.20.6",
                8, this.height - 16, COL_GREY, false);

        // Profile — top right
        if (client.getSession() != null) {
            String name = client.getSession().getUsername();
            int tw = textRenderer.getWidth(name);
            // small dark pill behind name
            ctx.fill(this.width - tw - 22, 12,
                     this.width - 8, 24, 0x88000000);
            ctx.drawText(textRenderer, name,
                    this.width - tw - 14, 15, COL_WHITE, false);
        }

        // Buttons
        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderLogo(DrawContext ctx) {
        // "BETTER " (white) + "GUI" (red accent) — scaled up 2.5×
        String part1 = "BETTER ";
        String part2 = "GUI";

        float scale   = 2.5f;
        int p1w = (int)(textRenderer.getWidth(part1) * scale);
        int p2w = (int)(textRenderer.getWidth(part2) * scale);
        int totalW = p1w + p2w;

        int lx = cx - totalW / 2;
        int ly = this.height / 2 - 155;

        ctx.getMatrices().push();
        ctx.getMatrices().translate(lx, ly, 0f);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.drawText(textRenderer, part1, 0, 0, COL_WHITE,  true);
        ctx.drawText(textRenderer, part2, textRenderer.getWidth(part1), 0, COL_ACCENT, true);
        ctx.getMatrices().pop();

        // Subtitle line
        ctx.drawCenteredTextWithShadow(textRenderer,
                "The Custom Client Experience", cx, ly + 30, COL_GREY);

        // Thin accent divider
        int divY = ly + 44;
        ctx.fill(cx - 60, divY, cx + 60, divY + 1, 0x44FFFFFF);
    }

    // ── Misc ─────────────────────────────────────────────────────────────────

    @Override
    public boolean shouldCloseOnEsc() { return false; }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == GLFW.GLFW_KEY_F11) {
            client.getWindow().toggleFullscreen();
            return true;
        }
        return super.keyPressed(key, scan, mods);
    }
}
