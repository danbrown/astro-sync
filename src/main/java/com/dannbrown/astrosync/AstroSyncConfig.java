package com.dannbrown.astrosync;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class AstroSyncConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = AstroSync.MOD_ID + ".json";

    private String repoOwner = "";
    private String repoName = "";

    private static AstroSyncConfig instance = new AstroSyncConfig();

    public static void load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        if (Files.isRegularFile(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                AstroSyncConfig parsed = GSON.fromJson(reader, AstroSyncConfig.class);
                if (parsed != null) {
                    instance = parsed;
                    if (instance.repoOwner == null) {
                        instance.repoOwner = "";
                    }
                    if (instance.repoName == null) {
                        instance.repoName = "";
                    }
                }
            } catch (IOException e) {
                AstroSync.LOGGER.error("Failed to load config {}", path, e);
            }
        } else {
            save();
        }
    }

    public static void save() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
        try {
            Files.createDirectories(path.getParent());
            try (Writer writer = Files.newBufferedWriter(path)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            AstroSync.LOGGER.error("Failed to save config {}", path, e);
        }
    }

    public static String getRepoOwner() {
        return instance.repoOwner;
    }

    public static String getRepoName() {
        return instance.repoName;
    }
}
