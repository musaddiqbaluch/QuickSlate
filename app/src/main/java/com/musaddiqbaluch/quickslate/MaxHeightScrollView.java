
package com.musaddiqbaluch.quickslate;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;

/**
 * NestedScrollView with a configurable maximum height.
 * The height limit is applied during measurement, avoiding
 * delayed layout changes when the user starts scrolling.
 */
public class MaxHeightScrollView extends NestedScrollView {

    private int maxHeight;

    public MaxHeightScrollView(
            @NonNull Context context) {
        super(context);
    }

    public MaxHeightScrollView(
            @NonNull Context context,
            @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MaxHeightScrollView(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMaxHeight(int height) {
        maxHeight = Math.max(0, height);
        requestLayout();
    }

    @Override
    protected void onMeasure(
            int widthMeasureSpec,
            int heightMeasureSpec) {

        if (maxHeight > 0) {

            int mode = MeasureSpec.getMode(heightMeasureSpec);
            int size = MeasureSpec.getSize(heightMeasureSpec);

            int limit = maxHeight;

            if (mode != MeasureSpec.UNSPECIFIED) {
                limit = Math.min(limit, size);
            }

            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    limit,
                    MeasureSpec.AT_MOST
            );
        }

        super.onMeasure(
                widthMeasureSpec,
                heightMeasureSpec
        );
    }
}
