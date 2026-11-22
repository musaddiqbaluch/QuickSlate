package com.musaddiqbaluch.quickslate;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;

import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.appbar.MaterialToolbar;

import android.graphics.Color;
import android.view.Window;

import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.color.MaterialColors;

import com.google.android.material.color.DynamicColors;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "QuickSlate";

    static final int DEST_ACCESSIBILITY = 1;
    static final int DEST_APPS = 2;
    static final int DEST_DEFAULT_ASSISTANT = 3;
    static final int DEST_BATTERY = 4;
    static final int DEST_DEVELOPER = 5;
    static final int DEST_ALL_FILES = 6;
    static final int DEST_UNKNOWN_APPS = 7;
    static final int DEST_NOTIFICATIONS = 8;
    static final int DEST_USAGE_ACCESS = 9;
    static final int DEST_DEFAULT_APPS = 10;
    static final int DEST_MODIFY_SETTINGS = 11;
    static final int DEST_OVERLAY = 12;
    static final int DEST_STORAGE = 13;

    // Same order in both arrays: view id -> destination
    private static final int[] TILE_IDS = {
            R.id.tile_accessibility,
            R.id.tile_apps,
            R.id.tile_default_assistant,
            R.id.tile_battery,
            R.id.tile_developer,
            R.id.tile_all_files,
            R.id.tile_unknown_apps,
            R.id.tile_notifications,
            R.id.tile_usage_access,
            R.id.tile_default_apps,
            R.id.tile_modify_settings,
            R.id.tile_overlay,
            R.id.tile_storage
    };

    private static final int[] TILE_DESTINATIONS = {
            DEST_ACCESSIBILITY,
            DEST_APPS,
            DEST_DEFAULT_ASSISTANT,
            DEST_BATTERY,
            DEST_DEVELOPER,
            DEST_ALL_FILES,
            DEST_UNKNOWN_APPS,
            DEST_NOTIFICATIONS,
            DEST_USAGE_ACCESS,
            DEST_DEFAULT_APPS,
            DEST_MODIFY_SETTINGS,
            DEST_OVERLAY,
            DEST_STORAGE
    };

            @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivityIfAvailable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_main);

        updateSystemBars();
        applyInsets();
        bindTiles();
        bindToolbar();
    }

    private void bindTiles() {
        for (int i = 0; i < TILE_IDS.length; i++) {
            final int destination = TILE_DESTINATIONS[i];
            View tile = findViewById(TILE_IDS[i]);

            tile.setOnClickListener(new View.OnClickListener() {
                @Override
                                public void onClick(View view) {
                    Haptics.tap(view);
                    openSettings(destination);
                }
            });
            
            
        }
        
        
    }
    
        private void bindToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);

        toolbar.setOnMenuItemClickListener(
                new Toolbar.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.action_settings) {
                            openAppSettings();
                            return true;
                        }
                        return false;
                    }
                }
        );
    }

    private void openAppSettings() {
        // Replace with your settings screen.
        startActivity(new Intent(this, SettingsActivity.class));
    }
    
    

    // Top inset is handled by AppBarLayout (fitsSystemWindows).
    // Sides and bottom are applied to the scrolling content.
    private void applyInsets() {
        NestedScrollView scroll = findViewById(R.id.content_scroll);

        ViewCompat.setOnApplyWindowInsetsListener(
                scroll,
                new OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(
                            View view,
                            WindowInsetsCompat insets
                    ) {
                        Insets bars = insets.getInsets(
                                WindowInsetsCompat.Type.systemBars()
                                        | WindowInsetsCompat.Type.displayCutout()
                        );

                        view.setPadding(bars.left, 0, bars.right, bars.bottom);
                        return insets;
                    }
                }
        );

        ViewCompat.requestApplyInsets(scroll);
    }
    
        // Makes status bar and navigation bar icons readable on the current
    // wallpaper-driven surface color (light or dark).
    private void updateSystemBars() {
        Window window = getWindow();

                        int surfaceColor = MaterialColors.getColor(
                this,
                com.google.android.material.R.attr.colorSurface,
                Color.WHITE
        );

        boolean isLightSurface = MaterialColors.isColorLight(surfaceColor);

        // Bar colors are ignored from API 35 (edge-to-edge is enforced).
        if (Build.VERSION.SDK_INT < 35) {
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }

        // Stops the system from adding a translucent scrim behind
        // 3-button navigation, which hides or washes out the icons.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());

        // true = dark icons (for light surfaces), false = light icons.
        controller.setAppearanceLightStatusBars(isLightSurface);
        controller.setAppearanceLightNavigationBars(isLightSurface);
    }

    private void openSettings(int destination) {
        List<Intent> intents = createSettingsIntents(this, destination);

        for (int i = 0; i < intents.size(); i++) {
            Intent intent = intents.get(i);

            try {
                startActivity(intent);
                return;
            } catch (ActivityNotFoundException exception) {
                Log.w(TAG, "Settings activity unavailable: " + intent.getAction(), exception);
            } catch (SecurityException exception) {
                Log.w(TAG, "Settings activity access denied: " + intent.getAction(), exception);
            } catch (IllegalArgumentException exception) {
                Log.w(TAG, "Invalid settings intent: " + intent.getAction(), exception);
            }
        }

        Toast.makeText(this, R.string.settings_unavailable, Toast.LENGTH_SHORT).show();
    }

    /*
     * Shared destination builder.
     * MainActivity and QuickSettingsTiles both use this method.
     */
    static List<Intent> createSettingsIntents(
            Context context,
            int destination
    ) {

        List<Intent> intents = new ArrayList<>();

        switch (destination) {

            case DEST_ACCESSIBILITY:
                addAction(intents, Settings.ACTION_ACCESSIBILITY_SETTINGS);
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_APPS:
                addAction(intents, Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS);
                addAction(intents, Settings.ACTION_APPLICATION_SETTINGS);
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_DEFAULT_ASSISTANT:
                // No public intent for the Digital assistant row, so open Default apps.
                addAction(intents, Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_BATTERY:
                addAction(intents, Intent.ACTION_POWER_USAGE_SUMMARY);
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_DEVELOPER:
                addAction(intents, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_ALL_FILES:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // System-wide list. Do NOT attach a package URI.
                    addAction(intents, Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                } else {
                    addAction(intents, Settings.ACTION_SETTINGS);
                }
                break;

            case DEST_UNKNOWN_APPS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // No package URI: opens the system-wide list.
                    addAction(intents, Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                } else {
                    addAction(intents, Settings.ACTION_SECURITY_SETTINGS);
                }
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

             case DEST_NOTIFICATIONS:
                // System-wide Notifications settings (not Quick Slate's own).
                // These two actions are not public SDK constants, so they are
                // used as strings and fall through to the next one if missing.
                addAction(intents, "android.settings.NOTIFICATION_SETTINGS");
                addAction(intents, "android.settings.ALL_APPS_NOTIFICATION_SETTINGS");
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_USAGE_ACCESS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    addAction(intents, Settings.ACTION_USAGE_ACCESS_SETTINGS);
                }
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_DEFAULT_APPS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addAction(intents, Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
                }
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            case DEST_MODIFY_SETTINGS:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // No package URI: opens the system-wide list.
                    addAction(intents, Settings.ACTION_MANAGE_WRITE_SETTINGS);
                } else {
                    addAction(intents, Settings.ACTION_SETTINGS);
                }
                break;

            case DEST_OVERLAY:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    // No package URI: opens the system-wide list.
                    addAction(intents, Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                } else {
                    addAction(intents, Settings.ACTION_SETTINGS);
                }
                break;

            case DEST_STORAGE:
                addAction(intents, Settings.ACTION_INTERNAL_STORAGE_SETTINGS);
                addAction(intents, Settings.ACTION_SETTINGS);
                break;

            default:
                addAction(intents, Settings.ACTION_SETTINGS);
                break;
        }

        return intents;
    }

    private static void addAction(
            List<Intent> intents,
            String action
    ) {
        intents.add(new Intent(action));
    }
}