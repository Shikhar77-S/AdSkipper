package com.adskipper.app;
import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.PowerManager;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.content.Context;
import java.util.List;
public class AdSkipperAccessibilityService extends AccessibilityService {
    private PowerManager.WakeLock wakeLock;
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String[] SKIP_TEXTS = {"Skip Ad","Skip Ads","Skip ad","skip ad","विज्ञापन छोड़ें","छोड़ें","SKIP","Skip","skip"};
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AdSkipper::WakeLock");
        wakeLock.acquire();
        startContinuousCheck();
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        String pkg = event.getPackageName() != null ? event.getPackageName().toString() : "";
        if (!pkg.contains("youtube")) return;
        findAndClickSkipButton();
    }
    private void startContinuousCheck() {
        handler.postDelayed(new Runnable() {
            @Override public void run() {
                findAndClickSkipButton();
                handler.postDelayed(this, 500);
            }
        }, 500);
    }
    private void findAndClickSkipButton() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) return;
        for (String skipText : SKIP_TEXTS) {
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(skipText);
            if (nodes != null && !nodes.isEmpty()) {
                for (AccessibilityNodeInfo node : nodes) {
                    if (node.isVisibleToUser()) { clickNode(node); return; }
                }
            }
        }
        String[] skipIds = {"com.google.android.youtube:id/skip_ad_button","com.google.android.youtube:id/skip_button","com.google.android.apps.youtube.music:id/skip_ad_button"};
        for (String skipId : skipIds) {
            List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(skipId);
            if (nodes != null && !nodes.isEmpty()) { clickNode(nodes.get(0)); return; }
        }
    }
    private void clickNode(AccessibilityNodeInfo node) {
        boolean clicked = node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
        if (!clicked) { AccessibilityNodeInfo parent = node.getParent(); if (parent != null) parent.performAction(AccessibilityNodeInfo.ACTION_CLICK); }
        if (!clicked) { Rect bounds = new Rect(); node.getBoundsInScreen(bounds); performTap(bounds.centerX(), bounds.centerY()); }
    }
    private void performTap(int x, int y) {
        Path path = new Path(); path.moveTo(x, y);
        GestureDescription gesture = new GestureDescription.Builder().addStroke(new GestureDescription.StrokeDescription(path, 0, 50)).build();
        dispatchGesture(gesture, null, null);
    }
    @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        super.onDestroy();
        if (wakeLock != null && wakeLock.isHeld()) wakeLock.release();
        handler.removeCallbacksAndMessages(null);
    }
}
