package com.dannbrown.astrosync.core;

import com.dannbrown.astrosync.AstroSyncLog;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Optional root {@code manifest.json} in the release zip: JSON array of
 * {@code {"filename":"mods/foo.jar","action":"delete"}} (paths relative to game directory).
 */
public final class UpdateManifest {

  private static final String MANIFEST_ENTRY = "manifest.json";
  private static final Gson GSON = new Gson();
  private static final Type LIST_TYPE = new TypeToken<List<ManifestAction>>() {}.getType();

  private UpdateManifest() {
  }

  static final class ManifestAction {
    String filename;
    String action;
  }

  /**
   * Reads delete targets from root {@code manifest.json}. Missing or invalid manifest yields empty list.
   */
  public static List<String> readDeleteTargets(ZipFile zip) {
    ZipEntry entry = zip.getEntry(MANIFEST_ENTRY);
    if (entry == null || entry.isDirectory()) {
      return Collections.emptyList();
    }
    try (Reader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
      List<ManifestAction> actions = GSON.fromJson(reader, LIST_TYPE);
      if (actions == null) {
        return Collections.emptyList();
      }
      List<String> out = new java.util.ArrayList<>();
      for (ManifestAction a : actions) {
        if (a == null) {
          continue;
        }
        String action = a.action != null ? a.action.trim().toLowerCase() : "";
        if (!"delete".equals(action)) {
          if (a.action != null && !a.action.isEmpty()) {
            AstroSyncLog.warn("manifest.json: unknown action '{}', skipping entry", a.action);
          }
          continue;
        }
        if (a.filename == null || a.filename.isBlank()) {
          AstroSyncLog.warn("manifest.json: delete entry missing filename, skipping");
          continue;
        }
        out.add(normalizeRelativePath(a.filename));
      }
      return out;
    } catch (JsonSyntaxException e) {
      AstroSyncLog.error("manifest.json: invalid JSON", e);
      return Collections.emptyList();
    } catch (Exception e) {
      AstroSyncLog.error("manifest.json: failed to read", e);
      return Collections.emptyList();
    }
  }

  static String normalizeRelativePath(String raw) {
    return raw.replace('\\', '/').replaceFirst("^\\./", "");
  }

  static boolean isRootManifestEntry(String zipEntryName) {
    return MANIFEST_ENTRY.equals(zipEntryName.replace('\\', '/'));
  }
}
