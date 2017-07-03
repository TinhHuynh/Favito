package com.huynhtinh1997.favito.preferences;

import android.content.Context;

/**
 * Created by huynhtinh1997 on 01/07/2017.
 */

public class SharedPreference {
    private static final String SHARED_PREFERENCES = "SharedPreference";
    private static final int MODE_SHARED_PREFERENCE = 1;
    private static final String PREFERENCE_PROFILE_NAME = "Profile Name";


    public static void saveProfileName(Context context, String profileName) {
        context.getSharedPreferences(SHARED_PREFERENCES, MODE_SHARED_PREFERENCE)
                .edit()
                .putString(PREFERENCE_PROFILE_NAME, profileName)
                .apply();
    }

    public static String getSavedProfileName(Context context) {
        return context.getSharedPreferences(SHARED_PREFERENCES, MODE_SHARED_PREFERENCE)
                .getString(PREFERENCE_PROFILE_NAME, null);
    }


}
