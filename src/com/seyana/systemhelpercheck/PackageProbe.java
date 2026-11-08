package com.seyana.systemhelpercheck;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/** Queries current-user registration; never mistakes a retained system APK for installation. */
public final class PackageProbe {
    public static final String TARGET = "com.mobiletools.systemhelper";
    private PackageProbe() {}
    public static final class Result {
        public final int state;
        public final String versionName;
        public final long versionCode;
        public final boolean enabled;
        public final boolean suspended;
        public final String detail;
        private Result(int state, String name, long code, boolean enabled, boolean suspended, String detail) {
            this.state = state;
            this.versionName = name;
            this.versionCode = code;
            this.enabled = enabled;
            this.suspended = suspended;
            this.detail = detail;
        }
    }
    @SuppressWarnings("deprecation")
    public static Result check(Context context, String packageName) {
        try {
            // No MATCH_UNINSTALLED_PACKAGES: system APK presence alone must not count.
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            ApplicationInfo app = info.applicationInfo;
            if (app == null) return new Result(2, null, 0, false, false, "系统没有返回应用状态，请重试。");
            if ((app.flags & ApplicationInfo.FLAG_INSTALLED) == 0)
                return new Result(0, null, 0, false, false, "当前用户未安装此组件。");
            long version = android.os.Build.VERSION.SDK_INT >= 28 ? info.getLongVersionCode() : info.versionCode;
            return new Result(1, info.versionName, version, app.enabled,
                    (app.flags & ApplicationInfo.FLAG_SUSPENDED) != 0, "当前用户已安装此组件。");
        } catch (PackageManager.NameNotFoundException absent) {
            return new Result(0, null, 0, false, false, "当前用户未安装此组件。");
        } catch (RuntimeException failure) {
            return new Result(2, null, 0, false, false, "系统查询失败（" + failure.getClass().getSimpleName() + "），请重试。");
        }
    }
}
