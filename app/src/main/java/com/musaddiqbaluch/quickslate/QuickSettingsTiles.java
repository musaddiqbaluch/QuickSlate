
package com.musaddiqbaluch.quickslate;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public final class QuickSettingsTiles {

    private static final String TAG = "QuickSlateTiles";

    private QuickSettingsTiles() {
        // Utility container.
    }

    public abstract static class BaseTileService extends TileService {

        protected abstract int getDestination();

        protected abstract int getLabelResource();

        protected abstract int getIconResource();

        @Override
        public void onStartListening() {
            super.onStartListening();
            updateTile();
        }

        private void updateTile() {

            Tile tile = getQsTile();

            if (tile == null) {
                return;
            }

            String label = getString(getLabelResource());

            tile.setLabel(label);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.setContentDescription(label);
            }

            tile.setIcon(
                    Icon.createWithResource(
                            this,
                            getIconResource()
                    )
            );

            // These are shortcuts, not state-changing toggles.
            tile.setState(Tile.STATE_INACTIVE);

            tile.updateTile();
        }

        @Override
        public void onClick() {
            super.onClick();

            List<Intent> intents =
                    MainActivity.createSettingsIntents(
                            this,
                            getDestination()
                    );

            for (int i = 0; i < intents.size(); i++) {

                Intent intent = intents.get(i);

                intent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                );

                try {

                    if (Build.VERSION.SDK_INT >= 34) {

                        PendingIntent pendingIntent =
                                PendingIntent.getActivity(
                                        this,
                                        getDestination(),
                                        intent,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                                | PendingIntent.FLAG_IMMUTABLE
                                );

                        startActivityAndCollapse(pendingIntent);

                    } else {

                        startActivityAndCollapse(intent);
                    }

                    return;

                } catch (ActivityNotFoundException exception) {

                    Log.w(
                            TAG,
                            "Settings screen unavailable: "
                                    + intent.getAction(),
                            exception
                    );

                } catch (SecurityException exception) {

                    Log.w(
                            TAG,
                            "Settings screen access denied: "
                                    + intent.getAction(),
                            exception
                    );

                } catch (IllegalArgumentException exception) {

                    Log.w(
                            TAG,
                            "Invalid settings intent: "
                                    + intent.getAction(),
                            exception
                    );

                } catch (IllegalStateException exception) {

                    Log.w(
                            TAG,
                            "Could not launch settings screen: "
                                    + intent.getAction(),
                            exception
                    );
                }
            }

            Toast.makeText(
                    this,
                    R.string.settings_unavailable,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    public static final class AccessibilityTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_ACCESSIBILITY;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_accessibility;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_accessibility;
        }
    }

    public static final class AppsTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_APPS;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_apps;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_apps;
        }
    }

    public static final class DefaultAssistantTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_DEFAULT_ASSISTANT;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_default_assistant;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_assistant;
        }
    }

    public static final class BatteryTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_BATTERY;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_battery;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_battery;
        }
    }

    public static final class DeveloperTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_DEVELOPER;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_developer;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_developer_mode;
        }
    }

    public static final class AllFilesTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_ALL_FILES;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_all_files;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_files;
        }
    }

    public static final class UnknownAppsTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_UNKNOWN_APPS;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_unknown_apps;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_install_mobile;
        }
    }

    public static final class NotificationsTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_NOTIFICATIONS;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_notifications;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_notification_settings;
        }
    }

    public static final class UsageAccessTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_USAGE_ACCESS;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_usage_access;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_query_stats;
        }
    }

    public static final class DefaultAppsTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_DEFAULT_APPS;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_default_apps;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_rocket_launch;
        }
    }

    public static final class ModifySettingsTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_MODIFY_SETTINGS;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_modify_settings;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_settings_modified;
        }
    }

    public static final class OverlayTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_OVERLAY;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_overlay;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_settings_overlay;
        }
    }

    public static final class StorageTile extends BaseTileService {

        @Override
        protected int getDestination() {
            return MainActivity.DEST_STORAGE;
        }

        @Override
        protected int getLabelResource() {
            return R.string.qs_storage;
        }

        @Override
        protected int getIconResource() {
            return R.drawable.ic_storage;
        }
    }
}
