package com.seyana.systemhelpercheck;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class MainActivity extends Activity {
    private static final int INK = 0xff18332e, MUTED = 0xff5d7069, TEAL = 0xff176c5b;
    private TextView badge, headline, explanation, checkedAt, version, history;
    private LinearLayout status;
    private Button details;
    private SharedPreferences prefs;
    private int dp(float value) { return (int) (value * getResources().getDisplayMetrics().density + .5f); }
    private GradientDrawable round(int color, int radius) {
        GradientDrawable bg = new GradientDrawable(); bg.setColor(color); bg.setCornerRadius(dp(radius)); return bg;
    }
    private TextView text(String content, int sp, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(content); t.setTextSize(sp); t.setTextColor(color);
        t.setIncludeFontPadding(false); t.setLineSpacing(dp(3), 1f);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t;
    }
    private void add(LinearLayout parent, View child, int top) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.topMargin = dp(top); parent.addView(child, lp);
    }
    private LinearLayout column(int padding) {
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(dp(padding),dp(padding),dp(padding),dp(padding)); return l;
    }
    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        prefs = getSharedPreferences("local_checks", MODE_PRIVATE);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setStatusBarColor(0xfff5f7f4); getWindow().setNavigationBarColor(0xfff5f7f4);
        final ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(0xfff5f7f4);
        scroll.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                return insets.consumeSystemWindowInsets();
            }
        });
        LinearLayout page = column(24); scroll.addView(page); setContentView(scroll); scroll.requestApplyInsets();
        add(page, text("本机检测  /  无需 ROOT", 12, TEAL, true), 8);
        add(page, text("组件检查", 30, INK, true), 14);
        add(page, text("SystemHelper 安装状态", 16, MUTED, false), 8);
        status = column(24); status.setBackground(round(0xffe7f3ed, 24)); add(page, status, 28);
        badge = text("当前用户", 12, TEAL, true); add(status, badge, 0);
        headline = text("正在检查", 34, INK, true); headline.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); add(status, headline, 14);
        explanation = text("", 16, INK, false); add(status, explanation, 14);
        checkedAt = text("", 12, MUTED, false); add(status, checkedAt, 20);
        LinearLayout target = column(20); target.setBackground(round(Color.WHITE, 20)); add(page, target, 16);
        add(target, text("检测对象", 13, MUTED, true), 0);
        TextView pkg = text(PackageProbe.TARGET, 15, INK, false); pkg.setTypeface(Typeface.MONOSPACE); pkg.setTextIsSelectable(true); add(target, pkg, 10);
        version = text("", 13, MUTED, false); add(target, version, 10);
        history = text("", 13, TEAL, false); add(target, history, 10);
        Button refresh = new Button(this); refresh.setText("重新检查"); refresh.setTextSize(16); refresh.setAllCaps(false); refresh.setTextColor(Color.WHITE); refresh.setBackground(round(TEAL, 16)); refresh.setMinHeight(dp(56));
        refresh.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) { refresh(); Toast.makeText(MainActivity.this,"已更新检查结果",Toast.LENGTH_SHORT).show(); } });
        add(page, refresh, 24);
        details = new Button(this); details.setText("打开组件详情"); details.setTextSize(15); details.setAllCaps(false); details.setTextColor(TEAL); details.setBackground(round(0xffe7eee8, 16)); details.setMinHeight(dp(52));
        details.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:"+PackageProbe.TARGET))); }
            catch (ActivityNotFoundException e) { Toast.makeText(MainActivity.this,"系统未提供组件详情页面",Toast.LENGTH_LONG).show(); }
            catch (SecurityException e) { Toast.makeText(MainActivity.this,"系统不允许打开此页面",Toast.LENGTH_LONG).show(); }
        }}); add(page, details, 12);
        add(page, text("如何理解结果", 14, INK, true), 28);
        add(page, text("仅检查当前用户。手机分身或其他用户需在对应空间检查。系统分区保留的原始 APK 不算当前用户已安装。", 13, MUTED, false), 10);
        add(page, text("打开应用、返回应用或点击按钮时更新；不在后台持续监控。检查记录仅保存在本机。", 13, MUTED, false), 12);
        add(page, text("离线运行 · 无权限申请 · v1.0", 12, MUTED, false), 24);
    }
    @Override protected void onResume() { super.onResume(); refresh(); }
    private String time(long value) { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(new Date(value)); }
    private void refresh() {
        PackageProbe.Result result = PackageProbe.check(this, PackageProbe.TARGET);
        long now = System.currentTimeMillis();
        checkedAt.setText("本次检查  " + time(now));
        int color = result.state == 0 ? TEAL : result.state == 1 ? 0xff8b570c : MUTED;
        int background = result.state == 0 ? 0xffe7f3ed : result.state == 1 ? 0xffffefda : 0xffe9ece9;
        status.setBackground(round(background,24)); headline.setTextColor(color); badge.setTextColor(color);
        if (result.state == 0) {
            headline.setText("未安装"); explanation.setText("当前用户未安装此组件。\n系统分区可能仍保留原始安装包。");
            prefs.edit().putLong("last_absent",now).apply();
            version.setVisibility(View.GONE);
        } else if (result.state == 1) {
            headline.setText("已安装");
            long lastAbsent = prefs.getLong("last_absent",0);
            explanation.setText(lastAbsent > 0 ? "此前检查为未安装，本次已检测到安装。组件已恢复到当前用户。" : "当前用户已安装此组件。\n仅凭首次检查无法判断是否为重新安装。");
            version.setVisibility(View.VISIBLE);
            version.setText("版本 " + (result.versionName == null ? "未知" : result.versionName) + " (" + result.versionCode + ")\n" + (result.enabled ? "已启用" : "已停用，但仍属于已安装") + (result.suspended ? " · 已暂停" : ""));
        } else {
            headline.setText("无法确认"); explanation.setText(result.detail); version.setVisibility(View.GONE);
        }
        details.setVisibility(result.state == 1 ? View.VISIBLE : View.GONE);
        long lastAbsent = prefs.getLong("last_absent",0);
        history.setVisibility(lastAbsent > 0 ? View.VISIBLE : View.GONE);
        history.setText(lastAbsent > 0 ? "最近确认未安装：\n" + time(lastAbsent) : "");
    }
}
