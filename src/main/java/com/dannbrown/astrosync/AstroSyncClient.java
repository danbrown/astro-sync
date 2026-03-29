package com.dannbrown.astrosync;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.client.Minecraft;

public class AstroSyncClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AstroSyncConfig.load();
        ClientLifecycleEvents.CLIENT_STARTED.register(c -> c.execute(AstroSyncClient::checkForUpdates));
    }

    private static void checkForUpdates() {
        UpdateApplyResult result = AstroSyncUpdate.checkAndApply();
        if (result == UpdateApplyResult.APPLIED) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                AstroSyncLog.warn("Update applied — closing game. Relaunch to load new files.");
                mc.stop();
            }
        }
    }
}
