
package com.musaddiqbaluch.quickslate;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.graphics.Insets;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.divider.MaterialDivider;

import java.util.List;

/** Bottom-aligned information dialog with fixed header and action button. */
public final class InfoDialog {

    private static final int TABLET_MAX_WIDTH_DP = 600;
    private static final int MAX_CONTENT_HEIGHT_DP = 440;

    private static final float MAX_DIALOG_HEIGHT_FRACTION = 0.94f;
    private static final float DIM_AMOUNT = 0.32f;

    public static final class Section {

        @NonNull
        public final String heading;

        @NonNull
        public final String body;

        public Section(
                @NonNull String heading,
                @NonNull String body) {

            this.heading = heading;
            this.body = body;
        }
    }

    private InfoDialog() {}

    public static boolean show(
            @Nullable Activity activity,
            @DrawableRes int iconRes,
            @NonNull String title,
            @NonNull List<Section> sections,
            @NonNull String buttonText) {

        if (activity == null
                || activity.isFinishing()
                || (Build.VERSION.SDK_INT >= 17 && activity.isDestroyed())) {
            return false;
        }

        try {
            final Context context = activity;

            float density = context.getResources()
                    .getDisplayMetrics().density;

            float fontScale = context.getResources()
                    .getConfiguration().fontScale;

            // Get the usable window dimensions.
            int availableWidth;
            int availableHeight;

            if (Build.VERSION.SDK_INT >= 30) {

                WindowMetrics metrics = activity.getWindowManager()
                        .getCurrentWindowMetrics();

                Insets bars = metrics.getWindowInsets()
                        .getInsetsIgnoringVisibility(
                                WindowInsets.Type.systemBars()
                                        | WindowInsets.Type.displayCutout());

                availableWidth = Math.max(
                        1,
                        metrics.getBounds().width() - bars.left - bars.right
                );

                availableHeight = Math.max(
                        1,
                        metrics.getBounds().height() - bars.top - bars.bottom
                );

            } else {

                android.util.DisplayMetrics metrics =
                        context.getResources().getDisplayMetrics();

                availableWidth = metrics.widthPixels;
                availableHeight = metrics.heightPixels;
            }

            boolean compact =
                    availableHeight / density < 620f
                            || fontScale >= 1.30f;

            int horizontalPadding = dp(context, compact ? 20 : 24);
            int topPadding = dp(context, compact ? 8 : 12);
            int bottomPadding = dp(context, compact ? 12 : 16);

            int maxDialogHeight = (int) (
                    availableHeight * MAX_DIALOG_HEIGHT_FRACTION
            );

            // Reserve enough space for the handle, header and button.
            float fixedContentDp =
                    (compact ? 168f : 198f)
                            + Math.max(0f, fontScale - 1f) * 80f;

            int maxScrollHeight = Math.max(
                    dp(context, 1),
                    Math.min(
                            dp(context, MAX_CONTENT_HEIGHT_DP),
                            maxDialogHeight - dp(context, fixedContentDp)
                    )
            );

            final int sheetColor = resolveThemeColor(
                    context,
                    com.google.android.material.R.attr.colorSurfaceContainerHigh,
                    resolveThemeColor(
                            context,
                            com.google.android.material.R.attr.colorSurface,
                            Color.WHITE
                    )
            );

            final int primaryColor = resolveThemeColor(
        context,
        androidx.appcompat.R.attr.colorPrimary,
        Color.BLACK
);

            final int onPrimaryColor = resolveThemeColor(
        context,
        com.google.android.material.R.attr.colorOnPrimary,
        Color.WHITE
);

            final Dialog dialog = new Dialog(context);

            dialog.setCancelable(true);
            dialog.setCanceledOnTouchOutside(true);

            View view = activity.getLayoutInflater().inflate(
                    R.layout.dialog_info,
                    null
            );

            // Root container.
            LinearLayout root = view.findViewById(
                    R.id.sheet_root
            );

            if (root == null && view instanceof LinearLayout) {
                root = (LinearLayout) view;
            }

            if (root == null) {
                dialog.dismiss();
                return false;
            }

            root.setPadding(
                    horizontalPadding,
                    topPadding,
                    horizontalPadding,
                    bottomPadding
            );

            // Rounded top corners, matching a modal sheet.
            GradientDrawable background = new GradientDrawable();
            background.setColor(sheetColor);

            float radius = dp(context, 28);

            background.setCornerRadii(new float[] {
                    radius, radius,
                    radius, radius,
                    0f, 0f,
                    0f, 0f
            });

            root.setBackground(background);
            root.setClipToOutline(true);

            // Header.
            ImageView icon = view.findViewById(
                    R.id.sheet_icon
            );

            TextView heading = view.findViewById(
                    R.id.sheet_title
            );

            icon.setImageResource(iconRes);
            icon.setImageTintList(
                    ColorStateList.valueOf(primaryColor)
            );

            heading.setText(title);

            // Scrollable sections.
            LinearLayout container = view.findViewById(
                    R.id.sheet_sections
            );

            for (int i = 0; i < sections.size(); i++) {

                if (i > 0) {
                    MaterialDivider divider =
                            new MaterialDivider(context);

                    container.addView(
                            divider,
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                    );
                }

                View block = activity.getLayoutInflater().inflate(
                        R.layout.item_info_section,
                        container,
                        false
                );

                TextView sectionHeading = block.findViewById(
                        R.id.section_heading
                );

                TextView sectionBody = block.findViewById(
                        R.id.section_body
                );

                sectionHeading.setText(sections.get(i).heading);
                sectionBody.setText(sections.get(i).body);

                container.addView(block);
            }

            // Apply the maximum height during measurement.
            MaxHeightScrollView scroll = view.findViewById(
                    R.id.sheet_scroll
            );

            scroll.setMaxHeight(maxScrollHeight);
            scroll.setFillViewport(false);
            scroll.setClipToPadding(false);
            scroll.setVerticalScrollBarEnabled(false);
            scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);

            // Fixed bottom action.
            MaterialButton button = view.findViewById(
                    R.id.sheet_button
            );

            button.setText(buttonText);
            button.setAllCaps(false);
            button.setTextColor(onPrimaryColor);
            button.setBackgroundTintList(
                    ColorStateList.valueOf(primaryColor)
            );

            button.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    }
            );

            dialog.setContentView(view);

            // Keep the dialog at the bottom instead of using
            // BottomSheetBehavior's separate positioning system.
            boolean tablet = context.getResources()
                    .getConfiguration().smallestScreenWidthDp >= 600;

            int dialogWidth = tablet
                    ? Math.min(
                            availableWidth,
                            dp(context, TABLET_MAX_WIDTH_DP)
                    )
                    : WindowManager.LayoutParams.MATCH_PARENT;

            applyWindow(
                    dialog.getWindow(),
                    dialogWidth,
                    sheetColor
            );

            dialog.show();

            // Reapply after show because Dialog may recalculate
            // its window dimensions during the show operation.
            applyWindow(
                    dialog.getWindow(),
                    dialogWidth,
                    sheetColor
            );

            return true;

        } catch (Exception e) {
            android.util.Log.e(
                    "InfoDialog",
                    "Unable to show information dialog",
                    e
            );

            return false;
        }
    }

    private static void applyWindow(
            @Nullable Window window,
            int width,
            int sheetColor) {

        if (window == null) return;

        window.setBackgroundDrawable(
                new ColorDrawable(Color.TRANSPARENT)
        );

        window.addFlags(
                WindowManager.LayoutParams.FLAG_DIM_BEHIND
        );

        window.setGravity(
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );

        window.setLayout(
                width,
                WindowManager.LayoutParams.WRAP_CONTENT
        );

        WindowManager.LayoutParams params =
                window.getAttributes();

        params.dimAmount = DIM_AMOUNT;
        window.setAttributes(params);

        window.setNavigationBarColor(sheetColor);
    }

    private static int resolveThemeColor(
            @NonNull Context context,
            int attribute,
            int fallback) {

        TypedValue value = new TypedValue();

        if (context.getTheme().resolveAttribute(
                attribute,
                value,
                true)) {

            if (value.resourceId != 0) {
                try {
                    return androidx.core.content.ContextCompat.getColor(
                            context,
                            value.resourceId
                    );
                } catch (Exception ignored) {
                }
            }

            if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                return value.data;
            }
        }

        return fallback;
    }

    private static int dp(
            @NonNull Context context,
            float value) {

        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                context.getResources().getDisplayMetrics()
        );
    }
}
