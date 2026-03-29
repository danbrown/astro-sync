package com.dannbrown.astrosync;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;

public class AstroSyncClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        AstroSyncConfig.load();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> client.execute(AstroSyncClient::checkForUpdates));
    }

    private static void checkForUpdates() {
        AstroSyncUpdate.checkAndApply();
    }
}
