package com.dannbrown.astrosync;

import com.dannbrown.astrosync.client.ModUpdateScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;

public class AstroSyncClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AstroSyncConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!AstroSync.needsUpdate || AstroSync.screenShown) {
                return;
            }
            if (client.screen instanceof TitleScreen title) {
                AstroSync.screenShown = true;
                client.setScreen(new ModUpdateScreen(title));
            }
        });

        ClientLifecycleEvents.CLIENT_STARTED.register(c -> c.execute(AstroSyncClient::checkForUpdates));
    }

    private static void checkForUpdates() {
        AstroSyncUpdate.checkAndApply();
        tryShowUpdateScreen(Minecraft.getInstance());
    }

    /**
     * If the title screen is already visible when the update check finishes, open the modal immediately.
     * Otherwise {@link ClientTickEvents#END_CLIENT_TICK} shows it on the next frame where the title is active.
     */
    private static void tryShowUpdateScreen(Minecraft client) {
        if (client == null || !AstroSync.needsUpdate || AstroSync.screenShown) {
            return;
        }
        if (client.screen instanceof TitleScreen title) {
            AstroSync.screenShown = true;
            client.setScreen(new ModUpdateScreen(title));
        }
    }
}
