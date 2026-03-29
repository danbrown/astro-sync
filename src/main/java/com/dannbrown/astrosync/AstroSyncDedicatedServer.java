package com.dannbrown.astrosync;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class AstroSyncDedicatedServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        AstroSyncConfig.load();
        ServerLifecycleEvents.SERVER_STARTED.register(AstroSyncDedicatedServer::onServerStarted);
    }

    private static void onServerStarted(MinecraftServer server) {
        if (!server.isDedicatedServer()) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                UpdateApplyResult result = AstroSyncUpdate.checkAndApply();
                if (result == UpdateApplyResult.APPLIED) {
                    Path gameDir = FabricLoader.getInstance().getGameDir();
                    server.execute(() -> DedicatedServerRestart.scheduleRestart(server, gameDir));
                }
            } catch (Exception e) {
                AstroSyncLog.error("Dedicated server update check failed", e);
            }
        });
    }
}
