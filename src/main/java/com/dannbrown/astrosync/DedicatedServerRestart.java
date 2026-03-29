package com.dannbrown.astrosync;

import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Attempts to relaunch the same JVM command after the dedicated server process exits.
 */
final class DedicatedServerRestart {

    private DedicatedServerRestart() {
    }

    static void scheduleRestart(MinecraftServer server, Path workingDir) {
        Optional<List<String>> command = buildRelaunchCommand();
        if (command.isEmpty()) {
            AstroSyncLog.error(
                    "Update applied but auto-restart is not available (could not read process command). Stop and start the server manually to load new files.");
            return;
        }

        List<String> cmd = command.get();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            try {
                new ProcessBuilder(cmd)
                        .directory(workingDir.toFile())
                        .inheritIO()
                        .start();
                AstroSyncLog.info("Replacement server process started.");
            } catch (IOException e) {
                AstroSyncLog.error("Failed to start replacement server process", e);
            }
        }, "astrosync-relaunch"));

        AstroSyncLog.warn("Update applied — stopping dedicated server for restart...");
        server.execute(() -> server.halt(false));
    }

    private static Optional<List<String>> buildRelaunchCommand() {
        ProcessHandle.Info info = ProcessHandle.current().info();
        Optional<String> executable = info.command();
        if (executable.isEmpty()) {
            return Optional.empty();
        }

        Optional<String[]> argArray = info.arguments();
        if (argArray.isPresent() && argArray.get().length > 0) {
            List<String> list = new ArrayList<>();
            list.add(executable.get());
            list.addAll(Arrays.asList(argArray.get()));
            return Optional.of(list);
        }

        Optional<String> line = info.commandLine();
        if (line.isEmpty()) {
            return Optional.empty();
        }

        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("win")) {
            return Optional.of(List.of("cmd.exe", "/c", line.get()));
        }
        return Optional.of(List.of("/bin/sh", "-c", line.get()));
    }
}
