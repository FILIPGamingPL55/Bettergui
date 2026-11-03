package com.bettergui.mixin;

import com.bettergui.BetterGuiMod;
import com.bettergui.cape.CapeManager;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Injects custom cape texture into the local player's SkinTextures.
 *
 * 1.21.11 SkinTextures record (Yarn mappings):
 *   SkinTextures(
 *     Identifier texture,
 *     @Nullable String textureUrl,
 *     @Nullable Identifier capeTexture,
 *     @Nullable Identifier elytraTexture,
 *     SkinTextures.Model model,
 *     boolean secure
 *   )
 */
@Mixin(AbstractClientPlayerEntity.class)
public class PlayerSkinMixin {

    @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
    private void onGetSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
        CapeManager cm = BetterGuiMod.CAPE_MANAGER;
        if (cm == null) return;

        String selected = cm.getSelectedCape();
        if (selected == null) return;

        Identifier capeId = cm.getCapeTexture(selected);
        if (capeId == null) return;

        SkinTextures orig = cir.getReturnValue();
        cir.setReturnValue(new SkinTextures(
            orig.texture(),
            orig.textureUrl(),
            capeId,               // ← custom cape
            orig.elytraTexture(),
            orig.model(),
            orig.secure()
        ));
    }
}
