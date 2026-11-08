package com.seyana.systemhelpercheck.test;

import android.app.Instrumentation;
import android.content.Context;
import android.content.ContextWrapper;
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
            result.putString("result", "PASS: installed, missing, query failure, actual target=" + actual.state);
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
