package com.seyana.systemhelpercheck;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
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
    private final ComponentCard[] cards = new ComponentCard[2];
    private TextView summary, checkedAt;
    private CheckHistory history;
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
        LinearLayout l = new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(dp(padding),dp(padding),dp(padding),dp(padding)); return l;
    }
    @Override public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        history = new CheckHistory(getSharedPreferences("local_checks", MODE_PRIVATE));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        getWindow().setStatusBarColor(0xfff5f7f4); getWindow().setNavigationBarColor(0xfff5f7f4);
        final ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(0xfff5f7f4);
        scroll.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @Override public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
                return insets.consumeSystemWindowInsets();
            }
        });
        LinearLayout page = column(22); scroll.addView(page); setContentView(scroll); scroll.requestApplyInsets();
        add(page, text("本机检测  /  无需 ROOT", 12, TEAL, true), 4);
        add(page, text("组件检查", 28, INK, true), 12);
        add(page, text("SystemHelper + RegService", 15, MUTED, false), 7);
        summary = text("正在检查 2 个组件", 15, TEAL, true); summary.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE); add(page, summary, 18);
        checkedAt = text("", 12, MUTED, false); add(page, checkedAt, 7);
        cards[0] = new ComponentCard("SystemHelper", "联通终端管理组件", PackageProbe.TARGET);
        cards[1] = new ComponentCard("RegService", "移动终端管理组件", PackageProbe.REG_SERVICE);
        add(page, cards[0].box, 20); add(page, cards[1].box, 14);
        Button refresh = new Button(this); refresh.setText("重新检查两个组件"); refresh.setTextSize(16); refresh.setAllCaps(false);
        refresh.setTextColor(Color.WHITE); refresh.setBackground(round(TEAL, 16)); refresh.setMinHeight(dp(54));
        refresh.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
            refresh(); Toast.makeText(MainActivity.this,"两个组件的结果已更新",Toast.LENGTH_SHORT).show();
        }}); add(page, refresh, 22);
        add(page, text("仅检查当前用户。系统分区保留的原始 APK 不算当前用户已安装；手机分身或其他用户需分别检查。", 12, MUTED, false), 20);
        add(page, text("打开、返回应用或点击按钮时更新。检查记录仅存本机，不持续后台监控。", 12, MUTED, false), 10);
        add(page, text("离线运行 · 无权限申请 · v1.1.0", 12, MUTED, false), 18);
    }
    @Override protected void onResume() { super.onResume(); refresh(); }
    private String time(long value) { return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault()).format(new Date(value)); }
    private void refresh() {
        long now = System.currentTimeMillis(); int installed = 0, absent = 0, unknown = 0;
        for (ComponentCard card : cards) {
            PackageProbe.Result result = PackageProbe.check(this, card.pkg);
            card.update(result, now);
            if (result.state == 1) installed++; else if (result.state == 0) absent++; else unknown++;
        }
        summary.setText("已安装 " + installed + "  ·  未安装 " + absent + "  ·  无法确认 " + unknown);
        checkedAt.setText("本次检查  " + time(now));
    }
    private final class ComponentCard {
        final String pkg;
        final LinearLayout box;
        final TextView state, explanation, version, record;
        final Button details;
        ComponentCard(String name, String description, String packageName) {
            pkg = packageName; box = column(18); box.setBackground(round(Color.WHITE,20));
            add(box, text(name, 21, INK, true),0);
            add(box, text(description,12,MUTED,false),5);
            TextView packageText=text(pkg,12,MUTED,false); packageText.setTypeface(Typeface.MONOSPACE); packageText.setTextIsSelectable(true); add(box,packageText,8);
            state=text("正在检查",26,TEAL,true); add(box,state,14);
            explanation=text("",14,INK,false); add(box,explanation,8);
            version=text("",12,MUTED,false); add(box,version,8);
            record=text("",11,MUTED,false); add(box,record,10);
            details=new Button(MainActivity.this); details.setText("打开组件详情"); details.setTextSize(14); details.setAllCaps(false);
            details.setTextColor(TEAL); details.setBackground(round(0xffe7eee8,12)); details.setMinHeight(dp(48));
            details.setOnClickListener(new View.OnClickListener() { @Override public void onClick(View v) {
                try { startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+pkg))); }
                catch (ActivityNotFoundException e) { Toast.makeText(MainActivity.this,"系统未提供组件详情页面",Toast.LENGTH_LONG).show(); }
                catch (SecurityException e) { Toast.makeText(MainActivity.this,"系统不允许打开此页面",Toast.LENGTH_LONG).show(); }
            }}); add(box,details,12);
        }
        void update(PackageProbe.Result result,long now) {
            int color=result.state==0?TEAL:result.state==1?0xff8b570c:MUTED;
            box.setBackground(round(result.state==0?0xffe7f3ed:result.state==1?0xffffefda:0xffe9ece9,20)); state.setTextColor(color);
            if (result.state==0) {
                state.setText("未安装"); explanation.setText("当前用户未安装此组件。"); history.recordAbsent(pkg,now);
            } else if (result.state==1) {
                state.setText("已安装");
                explanation.setText(history.lastAbsent(pkg)>0?"此前检查为未安装，本次检测到已恢复安装。":"当前用户已安装；首次检查无法判断安装时间。");
            } else { state.setText("无法确认"); explanation.setText(result.detail); }
            version.setVisibility(result.state==1?View.VISIBLE:View.GONE);
            if (result.state==1) version.setText("版本 "+(result.versionName==null?"未知":result.versionName)+" ("+result.versionCode+") · "+(result.enabled?"已启用":"已停用，仍属于已安装")+(result.suspended?" · 已暂停":""));
            long previous=history.lastAbsent(pkg); record.setVisibility(previous>0?View.VISIBLE:View.GONE);
            record.setText(previous>0?"最近确认未安装  "+time(previous):""); details.setVisibility(result.state==1?View.VISIBLE:View.GONE);
        }
    }
}
