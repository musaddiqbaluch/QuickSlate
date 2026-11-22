package com.musaddiqbaluch.quickslate;

import android.app.Activity;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/** The privacy policy, shown in the shared bottom sheet dialog. */
public final class PrivacyPolicyDialog {

    private PrivacyPolicyDialog() {}

    @MainThread
    public static void show(@Nullable Activity activity) {
        if (activity == null) return;

        List<InfoDialog.Section> sections = new ArrayList<>();
        sections.add(section(activity, R.string.privacy_overview_title, R.string.privacy_overview_body));
        sections.add(section(activity, R.string.privacy_internet_title, R.string.privacy_internet_body));
        sections.add(section(activity, R.string.privacy_permissions_title, R.string.privacy_permissions_body));
        sections.add(section(activity, R.string.privacy_data_title, R.string.privacy_data_body));
        sections.add(section(activity, R.string.privacy_tracking_title, R.string.privacy_tracking_body));
        sections.add(section(activity, R.string.privacy_links_title, R.string.privacy_links_body));
        sections.add(section(activity, R.string.privacy_changes_title, R.string.privacy_changes_body));

        InfoDialog.show(activity, R.drawable.ic_shield,
                activity.getString(R.string.privacy_title), sections,
                activity.getString(R.string.privacy_button));
    }

    private static InfoDialog.Section section(Activity activity, int title, int body) {
        return new InfoDialog.Section(activity.getString(title), activity.getString(body));
    }
}