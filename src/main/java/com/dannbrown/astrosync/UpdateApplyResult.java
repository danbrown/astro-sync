package com.dannbrown.astrosync;

/**
 * Outcome of {@link AstroSyncUpdate#checkAndApply()}.
 */
public enum UpdateApplyResult {
    SKIPPED_CONFIG,
    NO_UPDATE,
    FAILED,
    APPLIED
}
