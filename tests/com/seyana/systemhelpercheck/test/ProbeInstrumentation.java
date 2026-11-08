package com.seyana.systemhelpercheck.test;

import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import com.seyana.systemhelpercheck.CheckHistory;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.seyana.systemhelpercheck.PackageProbe;

public final class ProbeInstrumentation extends Instrumentation {
    private Bundle arguments;
    @Override public void onCreate(Bundle args) { arguments = args; start(); }
    @Override public void onStart() {
        Bundle result = new Bundle();
        try {
            Context target = getTargetContext();
            require(PackageProbe.check(target, target.getPackageName()).state == 1, "installed app");
            require(PackageProbe.check(target, "com.seyana.nonexistent.checkfixture.20261108").state == 0, "missing app");
            Context denied = new ContextWrapper(target) {
                @Override public PackageManager getPackageManager() { throw new SecurityException("fixture denied"); }
            };
            require(PackageProbe.check(denied, PackageProbe.TARGET).state == 2, "query failure must be unknown");
            int expected = Integer.parseInt(arguments.getString("expectedTarget", "0"));
            PackageProbe.Result actual = PackageProbe.check(target, PackageProbe.TARGET);
            require(actual.state == expected, "target state=" + actual.state + " expected=" + expected);
            int expectedReg = Integer.parseInt(arguments.getString("expectedRegService", "0"));
            PackageProbe.Result reg = PackageProbe.check(target, PackageProbe.REG_SERVICE);
            require(reg.state == expectedReg, "RegService state=" + reg.state + " expected=" + expectedReg);
            SharedPreferences fixture = target.getSharedPreferences("test_history_fixture", Context.MODE_PRIVATE);
            try {
                require(fixture.edit().clear().putLong("last_absent", 123L).commit(), "seed legacy history");
                CheckHistory history = new CheckHistory(fixture);
                require(history.lastAbsent(PackageProbe.TARGET) == 123L, "retain v1.0 history");
                require(history.lastAbsent(PackageProbe.REG_SERVICE) == 0L, "no legacy history on RegService");
                history.recordAbsent(PackageProbe.REG_SERVICE, 456L);
                require(history.lastAbsent(PackageProbe.TARGET) == 123L, "RegService must not overwrite SystemHelper");
                history.recordAbsent(PackageProbe.TARGET, 789L);
                require(history.lastAbsent(PackageProbe.REG_SERVICE) == 456L, "SystemHelper must not overwrite RegService");
                require(history.lastAbsent(PackageProbe.TARGET) == 789L, "store new SystemHelper baseline");
            } finally { fixture.edit().clear().commit(); }
            result.putString("result", "PASS: installed, missing, query failure, SystemHelper=" + actual.state + ", RegService=" + reg.state + "; legacy history and per-package isolation passed");
            finish(-1, result);
        } catch (Throwable failure) {
            result.putString("failure", failure.toString());
            finish(0, result);
        }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
