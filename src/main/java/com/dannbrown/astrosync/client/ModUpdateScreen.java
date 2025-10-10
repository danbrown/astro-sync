package com.dannbrown.astrosync.client;

import com.dannbrown.astrosync.AstroSync;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.NeoForge;

public class ModUpdateScreen extends Screen {
  private final Screen parent;

  public ModUpdateScreen(Screen parent) {
    super(Component.literal(AstroSync.NAME + " Screen"));
    this.parent = parent;
  }

  @Override
  protected void init() {
    Button buttonQuit = this.addRenderableWidget(
            Button.builder(Component.translatable("menu.quit"), button -> {
                      if (this.minecraft != null) {
                        this.minecraft.stop();
                      }
                    })
                    .width(128)
                    .build()
    );
    buttonQuit.setPosition(this.width / 2 - 64, this.height - 30);
  }


  @Override
  public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
    super.render(guiGraphics, mouseX, mouseY, partialTick);

    if (AstroSync.versionDownloaded) {
      guiGraphics.drawCenteredString(this.font,
              Component.translatable(AstroSync.MOD_ID + ".screen.title.success"),
              this.width / 2, 20, 0x00ff00);

      guiGraphics.drawCenteredString(this.font,
              Component.translatable(AstroSync.MOD_ID + ".screen.description.success"),
              this.width / 2, 40, 0xffffff);

      guiGraphics.drawCenteredString(this.font,
              Component.translatable(AstroSync.MOD_ID + ".screen.restart.success"),
              this.width / 2, 50, 0xffffff);
    } else {
      guiGraphics.drawCenteredString(this.font,
              Component.translatable(AstroSync.MOD_ID + ".screen.title.failure"),
              this.width / 2, 20, 0xff0000);

      guiGraphics.drawCenteredString(this.font,
              Component.translatable(AstroSync.MOD_ID + ".screen.description.failure"),
              this.width / 2, 40, 0xffffff);

      guiGraphics.drawCenteredString(this.font,
              Component.translatable(AstroSync.MOD_ID + ".screen.restart.failure"),
              this.width / 2, 50, 0xffffff);
    }

    guiGraphics.drawCenteredString(this.font,
            Component.translatable(AstroSync.MOD_ID + ".screen.version", AstroSync.newVersion),
            this.width / 2, 70, 0xffffff);

  }
}
