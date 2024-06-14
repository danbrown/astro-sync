package com.dannbrown.astrosync.client

import com.dannbrown.astrosync.AstroSyncMod
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

class ModUpdateScreen(parent: Screen) : Screen(Component.literal("${AstroSyncMod.NAME} Screen")) {
  private val parent: Screen = parent

  override fun init() {
    val buttonQuit: Button = this.addRenderableWidget(Button.builder(Component.translatable("menu.quit")) { button ->
      checkNotNull(this.minecraft)
      this.minecraft!!.stop()
    }
      .width(128)
      .build())
    buttonQuit.setPosition(this.width / 2 - 64, this.height - 30)
  }

  override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    this.renderBackground(guiGraphics)
    guiGraphics.drawCenteredString(this.font, Component.translatable("${AstroSyncMod.MOD_ID}.screen.title"), width / 2, 20, 0xff0000)
    guiGraphics.drawCenteredString(this.font, Component.translatable("${AstroSyncMod.MOD_ID}.screen.description"), width / 2, 20 + 20, 0xffffff)
    guiGraphics.drawCenteredString(this.font, Component.translatable("${AstroSyncMod.MOD_ID}.screen.restart"), width / 2, 20 + 30, 0xffffff)
    guiGraphics.drawCenteredString(this.font, Component.translatable("${AstroSyncMod.MOD_ID}.screen.version", AstroSyncMod.newVersion), width / 2, 20 + 50, 0xffffff)

    super.render(guiGraphics, mouseX, mouseY, partialTick)
  }
}