package com.huynhtinh1997.favito.animation;

import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by huynhtinh1997 on 09/07/2017.
 */

public class ActionBarShowHideAnimator {
    public static final long HIDE_DELAY = 3000;

    private ActionBar mActionBar;

    public ActionBarShowHideAnimator(AppCompatActivity activity) {
        mActionBar = activity.getSupportActionBar();
    }

    public void animate() {
        if (isActionBarShown()) {
            return;
        }
        mActionBar.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mActionBar.hide();
            }
        }, HIDE_DELAY);
    }

    private boolean isActionBarShown() {
        return mActionBar.isShowing();
    }
}
