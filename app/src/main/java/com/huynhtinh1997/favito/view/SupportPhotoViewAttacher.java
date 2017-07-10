package com.huynhtinh1997.favito.view;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.huynhtinh1997.favito.animation.ActionBarShowHideAnimator;

import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Created by huynhtinh1997 on 05/07/2017.
 */

class SupportPhotoViewAttacher extends PhotoViewAttacher {

    private GestureDetector mGestureDetector;
    private ActionBarShowHideAnimator mAnimator;

    SupportPhotoViewAttacher(ImageView imageView) {
        this(imageView, true);
    }

    private SupportPhotoViewAttacher(ImageView imageView, boolean zoomable) {
        super(imageView, zoomable);
        SupportPhotoView photoVIew = (SupportPhotoView) imageView;
        Fragment fragment = photoVIew.getHostFragment();
        if (fragment != null) {
            mGestureDetector = new GestureDetector(fragment.getActivity(), new SingleTapConfirm());
            mAnimator = new ActionBarShowHideAnimator((AppCompatActivity) fragment.getActivity());
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        if (mGestureDetector.onTouchEvent(ev)) {
            animateShowHideActionBar();
        } else {
            super.onTouch(v, ev);
        }
        return true;
    }

    private void animateShowHideActionBar() {
        mAnimator.animate();
    }

    private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            return true;
        }

    }

}
