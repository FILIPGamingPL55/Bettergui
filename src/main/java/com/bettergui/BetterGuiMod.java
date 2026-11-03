package com.bettergui;

import com.bettergui.cape.CapeManager;
import com.bettergui.event.GameEventHandler;
import com.bettergui.ntfy.NtfyClient;
import com.bettergui.ntfy.NtfyConfig;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterGuiMod implements ClientModInitializer {

    public static final String MOD_ID = "bettergui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static CapeManager CAPE_MANAGER;
    public static NtfyClient  NTFY;

    @Override
    public void onInitializeClient() {
        CAPE_MANAGER = new CapeManager();

        NtfyConfig cfg = NtfyConfig.load();
        NTFY = new NtfyClient(cfg);
        NTFY.start(); // nasłuchuje komend + gotowy do wysyłania

        GameEventHandler.register();

        LOGGER.info("[BetterGUI] Gotowy! ntfy topic: {}/{}", cfg.server, cfg.topic);
    }
}
