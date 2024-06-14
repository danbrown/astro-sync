package com.dannbrown.astrosync

import net.minecraftforge.common.ForgeConfigSpec

object AstroSyncConfig {
  val BUILDER: ForgeConfigSpec.Builder = ForgeConfigSpec.Builder()
  val SPEC: ForgeConfigSpec

  val RepoOwner: ForgeConfigSpec.ConfigValue<String>
  val RepoName: ForgeConfigSpec.ConfigValue<String>

  init {
    BUILDER.push("Common configs for ${AstroSyncMod.NAME} mod")

    RepoOwner = BUILDER.comment("The owner of the github repo to check for updates")
      .define("RepoOwner", "")
    RepoName = BUILDER.comment("The name of the github repo to check for updates")
      .define("RepoName", "")

    BUILDER.pop()
    SPEC = BUILDER.build()
  }
}
