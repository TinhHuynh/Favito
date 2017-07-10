package com.huynhtinh1997.favito.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;

import uk.co.senab.photoview.PhotoView;

/**
 * Created by huynhtinh1997 on 05/07/2017.
 */

public class SupportPhotoView extends PhotoView {

    private Fragment mHostFragment;

    public SupportPhotoView(Context context, AttributeSet attr) {
        super(context, attr);
    }

    public SupportPhotoView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);

    }

    public SupportPhotoView(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        super.init();
        SupportPhotoViewAttacher attacher = new SupportPhotoViewAttacher(this);
    }

    public Fragment getHostFragment() {
        return mHostFragment;
    }

    public void setHostFragment(Fragment hostFragment) {
        mHostFragment = hostFragment;
    }


}
