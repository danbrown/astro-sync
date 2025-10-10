package com.dannbrown.astrosync;

import net.neoforged.neoforge.common.ModConfigSpec;

public class AstroSyncConfig {

  public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
  public static final ModConfigSpec SPEC;

  public static final ModConfigSpec.ConfigValue<String> RepoOwner;
  public static final ModConfigSpec.ConfigValue<String> RepoName;

  static {
    BUILDER.push("Common configs for " + AstroSync.NAME + " mod");

    RepoOwner = BUILDER.comment("The owner of the github repo to check for updates")
            .define("RepoOwner", "");
    RepoName = BUILDER.comment("The name of the github repo to check for updates")
            .define("RepoName", "");

    BUILDER.pop();
    SPEC = BUILDER.build();
  }
}
