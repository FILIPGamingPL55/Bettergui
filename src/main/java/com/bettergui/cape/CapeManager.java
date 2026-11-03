package com.bettergui.cape;

import com.bettergui.BetterGuiMod;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages custom cape textures for the local player.
 *
 * Cape textures live at:
 *   src/main/resources/assets/bettergui/textures/cape/<name>.png
 *
 * Standard Minecraft cape texture size: 64 × 32 px
 * The cape area occupies pixels (0,0)→(64,32).
 * Layout matches vanilla: left arm sleeve, hood, body panel, right arm sleeve.
 *
 * To add your own cape:
 *   1. Drop a 64×32 PNG into textures/cape/
 *   2. Add its key to CAPE_NAMES below
 *   3. Add a display name entry to displayNames map
 */
public class CapeManager {

    /** Internal IDs used in filesystem / config */
    public static final String[] CAPE_NAMES = {
        "default",
        "flame",
        "galaxy",
        "gradient"
    };

    private static final Map<String, String> DISPLAY_NAMES = new LinkedHashMap<>();
    static {
        DISPLAY_NAMES.put("default",  "Default Cape");
        DISPLAY_NAMES.put("flame",    "Flame Cape");
        DISPLAY_NAMES.put("galaxy",   "Galaxy Cape");
        DISPLAY_NAMES.put("gradient", "Gradient Cape");
    }

    private final Map<String, Identifier> textureIds = new LinkedHashMap<>();
    private String selectedCape = "default";

    public CapeManager() {
        for (String name : CAPE_NAMES) {
            textureIds.put(name,
                new Identifier(BetterGuiMod.MOD_ID, "textures/cape/" + name + ".png"));
        }
        BetterGuiMod.LOGGER.info("[BetterGUI] CapeManager ready — {} capes loaded.", CAPE_NAMES.length);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns the Identifier for the currently selected cape, or null. */
    public Identifier getCapeTexture(String capeName) {
        if (capeName == null) return null;
        return textureIds.get(capeName);
    }

    /** Identifier for whatever cape is active right now. */
    public Identifier getActiveCapeTexture() {
        return getCapeTexture(selectedCape);
    }

    public String getSelectedCape() { return selectedCape; }

    /**
     * Set active cape. Pass {@code null} to disable the cape.
     */
    public void setSelectedCape(String cape) {
        if (cape != null && !textureIds.containsKey(cape)) {
            BetterGuiMod.LOGGER.warn("[BetterGUI] Unknown cape key: {}", cape);
            return;
        }
        this.selectedCape = cape;
        BetterGuiMod.LOGGER.info("[BetterGUI] Cape set to: {}", cape == null ? "none" : cape);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static String displayName(String key) {
        return DISPLAY_NAMES.getOrDefault(key,
            Character.toUpperCase(key.charAt(0)) + key.substring(1) + " Cape");
    }

    public Map<String, Identifier> getAllCapes() { return textureIds; }
}
