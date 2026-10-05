package com.dannbrown.astrosync;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AstroSync.MOD_ID, value = Dist.CLIENT)
public final class AstroSyncClient {

    private static boolean checked = false;

    private AstroSyncClient() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (checked) {
            return;
        }
        checked = true;
        checkForUpdates();
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
