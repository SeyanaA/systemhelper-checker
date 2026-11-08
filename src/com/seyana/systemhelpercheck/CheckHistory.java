package com.seyana.systemhelpercheck;

import android.content.SharedPreferences;

/** Independent per-package baselines, retaining v1.0 SystemHelper history only for its original package. */
public final class CheckHistory {
    private final SharedPreferences prefs;
    public CheckHistory(SharedPreferences prefs) { this.prefs = prefs; }
    public long lastAbsent(String pkg) {
        String key = "last_absent." + pkg;
        if (prefs.contains(key)) return prefs.getLong(key,0);
        return PackageProbe.TARGET.equals(pkg) ? prefs.getLong("last_absent",0) : 0;
    }
    public void recordAbsent(String pkg,long time) {
        prefs.edit().putLong("last_absent."+pkg,time).apply();
    }
}
