package com.dannbrown.astrosync.mixin;

import com.dannbrown.astrosync.AstroSyncMod;
import com.dannbrown.astrosync.client.ModUpdateScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Minecraft.class)
public class MinecraftMixin {
  @ModifyVariable(at = @At("HEAD"), method = "setScreen", argsOnly = true)
  private Screen showUpdateScreen(Screen variable) {
    if(!AstroSyncMod.screenShown && variable instanceof TitleScreen && AstroSyncMod.needsUpdate) {
      AstroSyncMod.screenShown = true;
      return new ModUpdateScreen(variable);
    }

    return variable;
  }
}