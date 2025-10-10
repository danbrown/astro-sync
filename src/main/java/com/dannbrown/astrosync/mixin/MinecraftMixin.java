package com.dannbrown.astrosync.mixin;

import com.dannbrown.astrosync.AstroSync;
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
  private Screen showUpdateScreen(Screen screen) {
    if(!AstroSync.screenShown && screen instanceof TitleScreen && AstroSync.needsUpdate) {
      AstroSync.screenShown = true;
      return new ModUpdateScreen(screen);
    }

    return screen;
  }
}