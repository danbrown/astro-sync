package com.dannbrown.astrosync.core;

import com.dannbrown.astrosync.AstroSync;
import net.neoforged.fml.ModList;
import net.neoforged.neoforgespi.language.IModFileInfo;
import net.neoforged.neoforgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Detects paths that refer to this mod's own loaded JAR. Writing or deleting those while the game is
 * running corrupts the JAR and breaks class loading.
 */
public final class SelfModPaths {

    private SelfModPaths() {
    }

    public static boolean isThisModFile(Path path) {
        if (path == null) {
            return false;
        }
        IModFileInfo modFileInfo = ModList.get().getModFileById(AstroSync.MOD_ID);
        if (modFileInfo == null) {
            return false;
        }
        IModFile modFile = modFileInfo.getFile();
        if (modFile == null) {
            return false;
        }
        Path root = modFile.getFilePath();
        if (root == null) {
            return false;
        }

        Path normalized = path.toAbsolutePath().normalize();
        try {
            Path r = root.toAbsolutePath().normalize();
            if (normalized.equals(r)) {
                return true;
            }
            if (Files.exists(normalized) && Files.exists(r) && Files.isSameFile(normalized, r)) {
                return true;
            }
        } catch (IOException ignored) {
            return false;
        }
        return false;
    }
}
