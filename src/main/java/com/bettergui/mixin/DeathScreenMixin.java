package com.bettergui.mixin;

import com.bettergui.event.GameEventHandler;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 1.21.1: DeathScreen constructor takes (Text message, boolean showScore).
 * We hook init() which fires after the screen is fully built.
 */
@Mixin(DeathScreen.class)
public class DeathScreenMixin {

    // W 1.21.1 pole nazywa się `deathMessage` w Yarn mappings
    @Inject(method = "init", at = @At("HEAD"))
    private void onDeathInit(CallbackInfo ci) {
        DeathScreen self = (DeathScreen)(Object)this;
        // Pobierz tytuł ekranu jako wiadomość śmierci (najlepsze przybliżenie client-side)
        String msg = self.getTitle() != null ? self.getTitle().getString() : "";
        GameEventHandler.onPlayerDied(msg);
    }
}
