package com.musaddiqbaluch.quickslate;

import android.animation.TimeInterpolator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/** No ripple. A long press shrinks the item; on release it springs back with a small bounce. */
public final class PressBounce {

    private static final float PRESSED_SCALE = 0.94f;
    private static final long PRESS_MS = 160L;
    private static final long RELEASE_MS = 500L;

    private static final TimeInterpolator PRESS = new DecelerateInterpolator();

    /** Damped spring: starts at 0, overshoots 1 a little, then settles on 1. */
    private static final TimeInterpolator SPRING = t ->
            (float) (1.0 - Math.exp(-5.0 * t) * Math.cos(3.0 * Math.PI * t));

    private PressBounce() {}

    @MainThread
    public static void attach(@NonNull View view, @Nullable final Runnable onClick) {
        view.setClickable(true);
        view.setFocusable(true);

        view.setOnClickListener(v -> {
            if (onClick != null) onClick.run();
        });

        // After a long press the view consumes it, so releasing doesn't also count as a tap
        view.setOnLongClickListener(v -> {
            shrink(v);
            return true;
        });

        view.setOnTouchListener((v, event) -> {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                release(v);
            }
            return false;   // the view still handles its own click and long press
        });
    }

    private static void shrink(@NonNull View v) {
        v.animate().cancel();
        v.animate()
                .scaleX(PRESSED_SCALE).scaleY(PRESSED_SCALE)
                .setDuration(PRESS_MS)
                .setInterpolator(PRESS)
                .start();
    }

    private static void release(@NonNull final View v) {
        if (v.getScaleX() >= 1f) return;   // it was a plain tap: nothing to bounce back

        float from = v.getScaleX();
        v.animate().cancel();
        v.setScaleX(from);
        v.setScaleY(from);

        // The spring overshoots, so animate "progress" and compute the scale from it
        android.animation.ValueAnimator animator =
                android.animation.ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(RELEASE_MS);
        animator.setInterpolator(SPRING);
        animator.addUpdateListener(a -> {
            float scale = from + (1f - from) * (float) a.getAnimatedValue();
            v.setScaleX(scale);
            v.setScaleY(scale);
        });
        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                v.setScaleX(1f);
                v.setScaleY(1f);
            }
        });
        animator.start();
    }
}