package com.bettergui.screen;

import com.bettergui.BetterGuiMod;
import com.bettergui.cape.CapeManager;
import com.bettergui.screen.widget.FeatherButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Cosmetics / Cape selection screen.
 *
 * Shows a 2-column grid of cape buttons. Selecting one applies it
 * to the local player via CapeManager. The preview renders the cape
 * texture (if found) as a small rectangle.
 */
public class CapeSelectionScreen extends Screen {

    private static final int COL_BG      = 0xF2101010;
    private static final int COL_WHITE   = 0xFFFFFFFF;
    private static final int COL_GREY    = 0xFFAAAAAA;
    private static final int COL_HIGHLIGHT = 0xFF4A80D0;

    private final Screen parent;
    private String hoveredCape = null;

    public CapeSelectionScreen(Screen parent) {
        super(Text.literal("Cosmetics — Capes"));
        this.parent = parent;
    }

    // ── Init ─────────────────────────────────────────────────────────────────

    @Override
    protected void init() {
        CapeManager cm = BetterGuiMod.CAPE_MANAGER;
        String[] capes = CapeManager.CAPE_NAMES;

        int cols   = 2;
        int btnW   = 180;
        int btnH   = 40;
        int gap    = 12;
        int gridW  = cols * btnW + (cols - 1) * gap;
        int startX = this.width / 2 - gridW / 2;
        int startY = 80;

        for (int i = 0; i < capes.length; i++) {
            String cape = capes[i];
            int col = i % cols;
            int row = i / cols;
            int bx  = startX + col * (btnW + gap);
            int by  = startY + row * (btnH + gap);

            FeatherButton.Style sty = cape.equals(cm.getSelectedCape())
                    ? FeatherButton.Style.PRIMARY
                    : FeatherButton.Style.DEFAULT;

            addDrawableChild(
                FeatherButton.builder(
                    Text.literal(CapeManager.displayName(cape)),
                    btn -> {
                        cm.setSelectedCape(cape);
                        // Rebuild buttons so selection highlight updates
                        clearChildren();
                        init();
                    }
                ).dimensions(bx, by, btnW, btnH).style(sty).build()
            );
        }

        // "No Cape" button
        int noCapeBtnY = startY + ((capes.length / cols) + 1) * (btnH + gap);
        FeatherButton.Style noStyle = cm.getSelectedCape() == null
                ? FeatherButton.Style.PRIMARY : FeatherButton.Style.DEFAULT;
        addDrawableChild(
            FeatherButton.builder(Text.literal("No Cape"), btn -> {
                cm.setSelectedCape(null);
                clearChildren(); init();
            }).dimensions(this.width / 2 - 95, noCapeBtnY, 190, btnH)
              .style(noStyle).build()
        );

        // Back button
        addDrawableChild(
            FeatherButton.builder(Text.literal("← Back"),
                btn -> client.setScreen(parent))
            .dimensions(this.width / 2 - 95, this.height - 55, 190, 40)
            .style(FeatherButton.Style.DEFAULT).build()
        );
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, this.width, this.height, COL_BG);

        // Title
        ctx.drawCenteredTextWithShadow(textRenderer, "COSMETICS — CAPES",
                this.width / 2, 24, COL_WHITE);
        ctx.drawCenteredTextWithShadow(textRenderer,
                "Select a cape for your character",
                this.width / 2, 38, COL_GREY);

        // Divider
        ctx.fill(this.width / 2 - 80, 52, this.width / 2 + 80, 53, 0x33FFFFFF);

        // Current selection label
        CapeManager cm = BetterGuiMod.CAPE_MANAGER;
        String current = cm.getSelectedCape() != null
                ? "Active: " + CapeManager.displayName(cm.getSelectedCape())
                : "Active: None";
        ctx.drawCenteredTextWithShadow(textRenderer, current,
                this.width / 2, 58, COL_HIGHLIGHT);

        super.render(ctx, mouseX, mouseY, delta);

        // Cape preview — show texture swatch next to title
        renderCapePreview(ctx, cm.getSelectedCape());
    }

    private void renderCapePreview(DrawContext ctx, String cape) {
        if (cape == null) return;
        Identifier tex = BetterGuiMod.CAPE_MANAGER.getCapeTexture(cape);
        if (tex == null) return;

        // Draw a 40×64 preview in the top-right corner
        int px = this.width - 70;
        int py = 20;
        ctx.fill(px - 2, py - 2, px + 42, py + 66, 0x44FFFFFF);
        ctx.drawTexture(tex, px, py, 0, 0, 40, 64, 40, 64);
    }
}
