package com.bettergui.mixin;

import com.bettergui.screen.CustomTitleScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Intercepts MinecraftClient.setScreen() and replaces the vanilla TitleScreen
 * with BetterGUI's CustomTitleScreen.
 *
 * A guard flag prevents the recursive call (setScreen → onSetScreen → setScreen)
 * from looping indefinitely.
 */
@Mixin(MinecraftClient.class)
public class TitleScreenMixin {

    private boolean bettergui_replacing = false;

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (!bettergui_replacing && screen instanceof TitleScreen) {
            bettergui_replacing = true;
            ((MinecraftClient)(Object)this).setScreen(new CustomTitleScreen());
            bettergui_replacing = false;
            ci.cancel();
        }
    }
}
