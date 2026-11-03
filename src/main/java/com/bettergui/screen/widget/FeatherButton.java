package com.bettergui.screen.widget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Custom button styled like Feather Client — dark, semi-transparent with
 * rounded corners, smooth hover animation, optional left accent stripe.
 */
public class FeatherButton extends ButtonWidget {

    public enum Style {
        DEFAULT,   // dark grey
        PRIMARY,   // blue accent
        DANGER     // red accent (for Quit)
    }

    // ── Colours ──────────────────────────────────────────────────────────────
    private static final int COL_DEFAULT_BG    = 0xBB1E1E1E;
    private static final int COL_DEFAULT_HOVER = 0xBB2E2E2E;
    private static final int COL_PRIMARY_BG    = 0xBB1A3560;
    private static final int COL_PRIMARY_HOVER = 0xBB204080;
    private static final int COL_DANGER_BG     = 0xBB5C1A1A;
    private static final int COL_DANGER_HOVER  = 0xBB7A2020;
    private static final int COL_BORDER        = 0x25FFFFFF;
    private static final int COL_TEXT          = 0xFFFFFFFF;
    private static final int COL_TEXT_DISABLED = 0xFF777777;
    private static final int COL_ACCENT_BLUE   = 0xFF4A80D0;
    private static final int COL_ACCENT_RED    = 0xFFD04040;

    private final Style style;
    private float hoverAnim = 0f;

    protected FeatherButton(int x, int y, int width, int height,
                            Text message, PressAction onPress, Style style) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
        this.style = style;
    }

    // ── Rendering ────────────────────────────────────────────────────────────

    @Override
    public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        boolean hovered = this.isHovered();

        // Smooth hover animation
        float speed = 0.12f;
        if (hovered) hoverAnim = Math.min(1f, hoverAnim + speed);
        else          hoverAnim = Math.max(0f, hoverAnim - speed);

        int bgCol, hvCol, accentCol;
        switch (style) {
            case PRIMARY -> { bgCol = COL_PRIMARY_BG; hvCol = COL_PRIMARY_HOVER; accentCol = COL_ACCENT_BLUE; }
            case DANGER  -> { bgCol = COL_DANGER_BG;  hvCol = COL_DANGER_HOVER;  accentCol = COL_ACCENT_RED;  }
            default      -> { bgCol = COL_DEFAULT_BG; hvCol = COL_DEFAULT_HOVER; accentCol = 0;               }
        }

        int finalBg = lerpColor(bgCol, hvCol, hoverAnim);
        int x = this.getX(), y = this.getY(), w = this.getWidth(), h = this.getHeight();

        // Background (pseudo-rounded by trimming corners)
        ctx.fill(x + 2, y,     x + w - 2, y + h,     finalBg);
        ctx.fill(x,     y + 2, x + 2,     y + h - 2, finalBg);
        ctx.fill(x + w - 2, y + 2, x + w, y + h - 2, finalBg);

        // Subtle border
        ctx.fill(x + 2, y,         x + w - 2, y + 1,     COL_BORDER);
        ctx.fill(x + 2, y + h - 1, x + w - 2, y + h,     COL_BORDER);
        ctx.fill(x,     y + 2,     x + 1,     y + h - 2, COL_BORDER);
        ctx.fill(x + w - 1, y + 2, x + w,     y + h - 2, COL_BORDER);

        // Left accent stripe for Primary / Danger
        if (style != Style.DEFAULT) {
            ctx.fill(x, y + 4, x + 3, y + h - 4, accentCol);
        }

        // Label
        TextRenderer font = MinecraftClient.getInstance().textRenderer;
        int textColor = this.active ? COL_TEXT : COL_TEXT_DISABLED;
        ctx.drawCenteredTextWithShadow(font, this.getMessage(),
                x + w / 2, y + (h - 8) / 2, textColor);
    }

    // ── Colour interpolation ─────────────────────────────────────────────────

    private static int lerpColor(int c1, int c2, float t) {
        int a = lerp((c1 >> 24) & 0xFF, (c2 >> 24) & 0xFF, t);
        int r = lerp((c1 >> 16) & 0xFF, (c2 >> 16) & 0xFF, t);
        int g = lerp((c1 >>  8) & 0xFF, (c2 >>  8) & 0xFF, t);
        int b = lerp( c1        & 0xFF,  c2        & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerp(int a, int b, float t) {
        return (int)(a + (b - a) * t);
    }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder(Text message, PressAction onPress) {
        return new Builder(message, onPress);
    }

    public static class Builder {
        private final Text message;
        private final PressAction onPress;
        private int x, y, w = 200, h = 20;
        private Style style = Style.DEFAULT;

        public Builder(Text message, PressAction onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder dimensions(int x, int y, int w, int h) {
            this.x = x; this.y = y; this.w = w; this.h = h;
            return this;
        }

        public Builder style(Style style) { this.style = style; return this; }

        public FeatherButton build() {
            return new FeatherButton(x, y, w, h, message, onPress, style);
        }
    }
}
