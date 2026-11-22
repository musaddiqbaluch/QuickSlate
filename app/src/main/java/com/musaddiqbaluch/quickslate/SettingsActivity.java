
package com.musaddiqbaluch.quickslate;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsActivity extends AppCompatActivity {

    private static final String SOURCE_CODE_URL =
            "https://github.com/musaddiqbaluch/QuickSlate";

    // Placeholder URL. Add a LICENSE file to the main branch later.
    private static final String LICENSE_URL =
            "https://github.com/musaddiqbaluch/QuickSlate/blob/main/LICENSE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivityIfAvailable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_settings);

        updateSystemBars();
        applyInsets();

        MaterialToolbar toolbar =
                findViewById(R.id.settings_toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        TextView versionValue = findViewById(R.id.version_value);
        versionValue.setText(versionName());

        bindHaptics();
        bindClicks();
    }

    private void bindClicks() {

        PressBounce.attach(findViewById(R.id.row_source_code), new Runnable() {
            @Override
            public void run() {
                openLink(SOURCE_CODE_URL);
            }
        });

        PressBounce.attach(findViewById(R.id.row_licenses), new Runnable() {
            @Override
            public void run() {
                openLink(LICENSE_URL);
            }
        });

        PressBounce.attach(findViewById(R.id.row_privacy), new Runnable() {
            @Override
            public void run() {
                PrivacyPolicyDialog.show(SettingsActivity.this);
            }
        });

        PressBounce.attach(findViewById(R.id.row_version), null);

        findViewById(R.id.btn_github).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openLink(SOURCE_CODE_URL);
                    }
                });
    }

    private void bindHaptics() {

        final MaterialSwitch toggle =
                findViewById(R.id.switch_haptics);

        final MaterialButtonToggleGroup group =
                findViewById(R.id.group_strength);

        boolean enabled = Haptics.isEnabled(this);

        toggle.setChecked(enabled);
        group.check(buttonForLevel(Haptics.getLevel(this)));

        setStrengthEnabled(group, enabled);

        toggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(
                            CompoundButton button,
                            boolean checked) {

                        Haptics.setEnabled(
                                SettingsActivity.this, checked);

                        setStrengthEnabled(group, checked);

                        if (checked) {
                            Haptics.preview(
                                    button,
                                    Haptics.getLevel(SettingsActivity.this));
                        }
                    }
                });

        // The whole row toggles the switch.
        PressBounce.attach(findViewById(R.id.row_haptics), new Runnable() {
            @Override
            public void run() {
                toggle.toggle();
            }
        });

        group.addOnButtonCheckedListener(
                new MaterialButtonToggleGroup.OnButtonCheckedListener() {
                    @Override
                    public void onButtonChecked(
                            MaterialButtonToggleGroup g,
                            int checkedId,
                            boolean isChecked) {

                        if (!isChecked) {
                            return;
                        }

                        int level = levelForButton(checkedId);

                        Haptics.setLevel(
                                SettingsActivity.this, level);

                        Haptics.preview(g, level);
                    }
                });
    }

    private void setStrengthEnabled(
            MaterialButtonToggleGroup group,
            boolean enabled) {

        group.setEnabled(enabled);

        for (int i = 0; i < group.getChildCount(); i++) {
            group.getChildAt(i).setEnabled(enabled);
        }
    }

    private int buttonForLevel(int level) {

        switch (level) {
            case Haptics.LIGHT:
                return R.id.btn_light;

            case Haptics.HEAVY:
                return R.id.btn_heavy;

            default:
                return R.id.btn_medium;
        }
    }

    private int levelForButton(int buttonId) {

        if (buttonId == R.id.btn_light) {
            return Haptics.LIGHT;
        }

        if (buttonId == R.id.btn_heavy) {
            return Haptics.HEAVY;
        }

        return Haptics.MEDIUM;
    }

    private void openLink(String url) {

        try {
            Intent intent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url));

            startActivity(intent);

        } catch (ActivityNotFoundException exception) {

            Toast.makeText(
                    this,
                    R.string.link_unavailable,
                    Toast.LENGTH_SHORT
            ).show();

        } catch (SecurityException exception) {

            Toast.makeText(
                    this,
                    R.string.link_unavailable,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @SuppressWarnings("deprecation")
    private String versionName() {

        try {
            PackageInfo info;

            if (Build.VERSION.SDK_INT >= 33) {
                info = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.PackageInfoFlags.of(0));
            } else {
                info = getPackageManager().getPackageInfo(
                        getPackageName(), 0);
            }

            return info.versionName != null
                    ? info.versionName
                    : "1.0";

        } catch (PackageManager.NameNotFoundException exception) {
            return "1.0";
        }
    }

    // Top inset is handled by AppBarLayout (fitsSystemWindows).
    private void applyInsets() {

        View scroll = findViewById(R.id.settings_scroll);

        ViewCompat.setOnApplyWindowInsetsListener(
                scroll,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(
                            View view,
                            WindowInsetsCompat insets) {

                        Insets bars = insets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                                        | WindowInsetsCompat.Type.displayCutout());

                        view.setPadding(
                                bars.left,
                                0,
                                bars.right,
                                bars.bottom);

                        return insets;
                    }
                });

        ViewCompat.requestApplyInsets(scroll);
    }

    private void updateSystemBars() {

        Window window = getWindow();

        int surfaceColor = MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorSurface,
                Color.WHITE);

        boolean isLightSurface =
                MaterialColors.isColorLight(surfaceColor);

        if (Build.VERSION.SDK_INT < 35) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(
                        window,
                        window.getDecorView());

        controller.setAppearanceLightStatusBars(isLightSurface);
        controller.setAppearanceLightNavigationBars(isLightSurface);
    }
}
