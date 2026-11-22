
package com.musaddiqbaluch.quickslate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;

public final class Haptics {

    public static final int LIGHT = 0;
    public static final int MEDIUM = 1;
    public static final int HEAVY = 2;

    private static final String PREFS = "quickslate_prefs";
    private static final String KEY_ENABLED = "haptics_enabled";
    private static final String KEY_LEVEL = "haptics_level";

    private static final long MEDIUM_GAP_MS = 32L;
    private static final long HEAVY_GAP_MS = 30L;

    // Android 8-9 amplitude values.
    private static final int[] AMPLITUDE = {
        64, 140, 255
    };

    private static final long[] DURATION_MS = {
        20L, 22L, 24L
    };

    // Android 8-9 and older fallback patterns.
    private static final long[] LIGHT_PATTERN = {
        0L, 20L
    };

    private static final long[] MEDIUM_PATTERN = {
        0L, 20L,
        45L, 20L
    };

    private static final long[] HEAVY_PATTERN = {
        0L, 22L,
        45L, 22L,
        45L, 22L
    };

    private static final Handler MAIN_HANDLER =
            new Handler(Looper.getMainLooper());

    private static Runnable pendingModernPulse;

    private Haptics() {
    }

    // ------------------------------------------------------------
    // Preferences
    // ------------------------------------------------------------

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(
                        PREFS,
                        Context.MODE_PRIVATE
                );
    }

    public static boolean isEnabled(Context context) {
        return prefs(context).getBoolean(
                KEY_ENABLED,
                false
        );
    }

    public static void setEnabled(
            Context context,
            boolean enabled) {

        prefs(context).edit()
                .putBoolean(KEY_ENABLED, enabled)
                .apply();
    }

    public static int getLevel(Context context) {
        return normalizeLevel(
                prefs(context).getInt(
                        KEY_LEVEL,
                        MEDIUM
                )
        );
    }

    public static void setLevel(
            Context context,
            int level) {

        prefs(context).edit()
                .putInt(
                        KEY_LEVEL,
                        normalizeLevel(level)
                )
                .apply();
    }

    private static int normalizeLevel(int level) {
        if (level < LIGHT || level > HEAVY) {
            return MEDIUM;
        }

        return level;
    }

    // ------------------------------------------------------------
    // Public methods
    // ------------------------------------------------------------

    /**
     * Plays saved haptic strength only when enabled.
     */
    public static void tap(final View view) {
        if (view == null) {
            return;
        }

        if (!isEnabled(view.getContext())) {
            return;
        }

        play(view, getLevel(view.getContext()));
    }

    /**
     * Plays a preview regardless of the enabled preference.
     */
    public static void preview(
            final View view,
            final int level) {

        if (view == null) {
            return;
        }

        play(view, normalizeLevel(level));
    }

    // ------------------------------------------------------------
    // Dispatcher
    // ------------------------------------------------------------

    private static void play(
            final View view,
            final int level) {

        if (Build.VERSION.SDK_INT >= 33) {

            runOnMain(new Runnable() {
                @Override
                public void run() {
                    playModern(view, level);
                }
            });

        } else {

            playLegacy(
                    view.getContext(),
                    view,
                    level
            );
        }
    }

    // ------------------------------------------------------------
    // Android 13+
    // ------------------------------------------------------------

    /**
     * Uses action-oriented View haptic feedback.
     *
     * Different pulse counts distinguish the three settings.
     * This intentionally respects system haptic settings.
     */
    private static void playModern(
            final View view,
            final int level) {

        if (!view.isShown()) {
            return;
        }

        cancelModernPulses();

        int pulseCount;

        switch (level) {
            case LIGHT:
                pulseCount = 1;
                break;

            case MEDIUM:
                pulseCount = 2;
                break;

            case HEAVY:
                pulseCount = 3;
                break;

            default:
                return;
        }

        // First pulse immediately.
        performModernPulse(view);

        if (pulseCount == 1) {
            return;
        }

        final int totalPulses = pulseCount;
        final int gap = level == MEDIUM
                ? (int) MEDIUM_GAP_MS
                : (int) HEAVY_GAP_MS;

        final class PulseSequence implements Runnable {

            private int completed = 1;

            @Override
            public void run() {

                if (!view.isShown()) {
                    pendingModernPulse = null;
                    return;
                }

                performModernPulse(view);
                completed++;

                if (completed < totalPulses) {
                    MAIN_HANDLER.postDelayed(
                            this,
                            gap
                    );
                } else {
                    pendingModernPulse = null;
                }
            }
        }

        Runnable sequence = new PulseSequence();

        pendingModernPulse = sequence;

        MAIN_HANDLER.postDelayed(
                sequence,
                gap
        );
    }

    private static void performModernPulse(View view) {
        try {
            view.performHapticFeedback(
                    HapticFeedbackConstants.CLOCK_TICK
            );
        } catch (RuntimeException ignored) {
            // Do not bypass system haptic settings.
        }
    }

    private static void cancelModernPulses() {
        if (pendingModernPulse != null) {
            MAIN_HANDLER.removeCallbacks(
                    pendingModernPulse
            );

            pendingModernPulse = null;
        }
    }

    // ------------------------------------------------------------
    // Android 10-12 and older
    // ------------------------------------------------------------

    private static void playLegacy(
            Context context,
            View view,
            int level) {

        Vibrator vibrator = getVibrator(context);

        if (vibrator == null) {
            return;
        }

        try {
            if (!vibrator.hasVibrator()) {
                return;
            }
        } catch (RuntimeException exception) {
            return;
        }

        try {
            vibrator.cancel();

            // Android 10-12: predefined effects.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                int effectId;

                switch (level) {
                    case LIGHT:
                        effectId = VibrationEffect.EFFECT_TICK;
                        break;

                    case MEDIUM:
                        effectId = VibrationEffect.EFFECT_CLICK;
                        break;

                    case HEAVY:
                        effectId =
                                VibrationEffect.EFFECT_HEAVY_CLICK;
                        break;

                    default:
                        return;
                }

                vibrator.vibrate(
                        VibrationEffect.createPredefined(effectId)
                );

                return;
            }

            // Android 8-9.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                if (vibrator.hasAmplitudeControl()) {

                    vibrator.vibrate(
                            VibrationEffect.createOneShot(
                                    DURATION_MS[level],
                                    AMPLITUDE[level]
                            )
                    );

                } else {

                    vibrator.vibrate(
                            VibrationEffect.createWaveform(
                                    getPattern(level),
                                    -1
                            )
                    );
                }

                return;
            }

            // Android 7 and older.
            vibrator.vibrate(
                    getPattern(level),
                    -1
            );

        } catch (RuntimeException exception) {

            // Older devices may still support View feedback.
            try {
                view.performHapticFeedback(
                        HapticFeedbackConstants.VIRTUAL_KEY
                );
            } catch (RuntimeException ignored) {
            }
        }
    }

    // ------------------------------------------------------------
    // Legacy pattern selector
    // ------------------------------------------------------------

    private static long[] getPattern(int level) {

        switch (level) {
            case LIGHT:
                return LIGHT_PATTERN;

            case MEDIUM:
                return MEDIUM_PATTERN;

            case HEAVY:
                return HEAVY_PATTERN;

            default:
                return LIGHT_PATTERN;
        }
    }

    // ------------------------------------------------------------
    // Vibrator service
    // ------------------------------------------------------------

    @SuppressWarnings("deprecation")
    private static Vibrator getVibrator(Context context) {

        Context app = context.getApplicationContext();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            VibratorManager manager =
                    (VibratorManager) app.getSystemService(
                            Context.VIBRATOR_MANAGER_SERVICE
                    );

            return manager != null
                    ? manager.getDefaultVibrator()
                    : null;
        }

        return (Vibrator) app.getSystemService(
                Context.VIBRATOR_SERVICE
        );
    }

    // ------------------------------------------------------------
    // Main thread helper
    // ------------------------------------------------------------

    private static void runOnMain(Runnable runnable) {

        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            MAIN_HANDLER.post(runnable);
        }
    }
}
