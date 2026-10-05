package com.dannbrown.astrosync;

import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = AstroSync.MOD_ID, value = Dist.DEDICATED_SERVER)
public final class AstroSyncDedicatedServer {

    private AstroSyncDedicatedServer() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!server.isDedicatedServer()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                UpdateApplyResult result = AstroSyncUpdate.checkAndApply();
                if (result == UpdateApplyResult.APPLIED) {
                    Path gameDir = FMLPaths.GAMEDIR.get();
                    server.execute(() -> DedicatedServerRestart.scheduleRestart(server, gameDir));
                }
            } catch (Exception e) {
                AstroSyncLog.error("Dedicated server update check failed", e);
            }
        });
    }
}
