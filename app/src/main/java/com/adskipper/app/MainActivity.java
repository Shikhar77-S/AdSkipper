package com.adskipper.app;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
public class MainActivity extends AppCompatActivity {
    private TextView statusText;
    private Button enableBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusText = findViewById(R.id.status_text);
        enableBtn = findViewById(R.id.enable_btn);
        enableBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(this, "Ad Skipper ko Enable Karen!", Toast.LENGTH_LONG).show();
        });
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        updateStatus();
    }
    private void updateStatus() {
        if (isAccessibilityServiceEnabled()) {
            statusText.setText("Ad Skipper Chalu Hai! YouTube ads skip honge!");
            enableBtn.setText("Service Chalu Hai");
            enableBtn.setEnabled(false);
        } else {
            statusText.setText("Service Band Hai. Enable Karen.");
            enableBtn.setText("Enable Karen");
            enableBtn.setEnabled(true);
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices =
            am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getId().contains("com.adskipper.app")) return true;
        }
        return false;
    }
}
