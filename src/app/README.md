# AEROMute | Advanced Audio Steering (Non-Root)

AEROMute is a precision audio redirection tool designed for standard Android devices. It implements a persistent floating shifter and hardware-aware automation inspired by the `FloatingMute` and `FloatingVolume` logic.

## 🚀 Native Implementation Guide

To achieve the "Always-on-Top" behavior and hardware detection in your final APK, follow these native implementation steps.

### 1. The Floating Overlay (SYSTEM_ALERT_WINDOW)
To show the shifter outside the app, you must implement an Android **Foreground Service**.

```java
// Inside your ForegroundService.java
WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
WindowManager.LayoutParams params = new WindowManager.LayoutParams(
    WindowManager.LayoutParams.WRAP_CONTENT,
    WindowManager.LayoutParams.WRAP_CONTENT,
    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, // Required for Android 8.0+
    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
    PixelFormat.TRANSLUCENT);

// Set position
params.gravity = Gravity.TOP | Gravity.LEFT;
params.x = 0;
params.y = 100;

windowManager.addView(floatingView, params);
```

### 2. Hardware Broadcast Receiver
Monitor 3.5mm jack events to trigger AERO auto-calibration.

```java
public class HeadsetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction())) {
            int state = intent.getIntExtra("state", -1);
            boolean plugged = (state == 1);
            
            // Bridge to Capacitor Web View
            String script = "window.dispatchEvent(new CustomEvent('headset_event', { detail: { plugged: " + plugged + " } }));";
            webView.evaluateJavascript(script, null);
        }
    }
}
```

### 3. Permissions Checklist
- `SYSTEM_ALERT_WINDOW`: For the floating shifter.
- `MODIFY_AUDIO_SETTINGS`: For the master volume control.
- `ACCESS_NOTIFICATION_POLICY`: To bypass Do Not Disturb if requested.

## 🛠 Features
- **Precision Shifter:** 500px vertical slider for granular gain control.
- **Hardware Link:** Real-time 3.5mm & Bluetooth state detection.
- **App Exclusion:** Whitelist critical apps (e.g., Phone, Dialer) to bypass steering.
