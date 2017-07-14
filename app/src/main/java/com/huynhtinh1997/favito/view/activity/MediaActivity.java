package com.huynhtinh1997.favito.view.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.ShareDialog;
import com.huynhtinh1997.favito.R;
import com.huynhtinh1997.favito.animation.ActionBarShowHideAnimator;
import com.huynhtinh1997.favito.model.FileHolder;
import com.huynhtinh1997.favito.utils.MediaUtils;
import com.huynhtinh1997.favito.view.SupportPhotoView;
import com.huynhtinh1997.favito.view.dialog.MediaDetailsDialog;

import java.io.File;
import java.util.ArrayList;


/**
 * Created by huynhtinh1997 on 03/07/2017.
 */

public class MediaActivity extends BaseActivity {
    private static final String EXTRA_FILE_HOLDERS = "file holders";
    private static final String EXTRA_POSITION = "position";

    private File mCurrentFile;
    private int mCurrentPosition;
    private ArrayList<FileHolder> mFileHolders;
    private MediaAdapter mAdapter;


    public static Intent newIntent(Context context, int position, ArrayList<FileHolder> fileHolders) {
        Intent intent = new Intent(context, MediaActivity.class);
        intent.putExtra(EXTRA_FILE_HOLDERS, fileHolders);
        intent.putExtra(EXTRA_POSITION, position);
        return intent;
    }

    protected File getCurrentFile() {
        return mCurrentFile;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        initActionBar();
        mFileHolders = (ArrayList<FileHolder>) getIntent().getSerializableExtra(EXTRA_FILE_HOLDERS);
        mCurrentPosition = getIntent().getIntExtra(EXTRA_POSITION, 0);
        initViewPager();
    }

    @Override
    protected void initShareDiagLog() {
        super.initShareDiagLog();
        getShareDiaLog().registerCallback(getCallbackManager(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                showToast("Ops! Something wrong!");
            }
        });
    }

    @Override
    protected void showNeccessaryMenuItems() {
        Menu optionMenu = getOptionMenu();
        optionMenu.findItem(R.id.share_to_fb).setVisible(true);
        optionMenu.findItem(R.id.detail).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.detail:
                new MediaDetailsDialog(this, mCurrentFile).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void shareMediaToFacebook() {
        if (MediaUtils.checkSupportPhotoFile(mCurrentFile)) {
            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setImageUrl(Uri.fromFile(getCurrentFile()))
                    .build();
            SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder()
                    .addPhoto(sharePhoto)
                    .build();
            getShareDiaLog().show(sharePhotoContent, ShareDialog.Mode.AUTOMATIC);
        } else if (MediaUtils.checkSupportVideoFile(mCurrentFile)) {
            ShareVideo shareVideo = new ShareVideo.Builder()
                    .setLocalUrl(Uri.fromFile(getCurrentFile()))
                    .build();
            ShareVideoContent shareVideoContent = new ShareVideoContent.Builder()
                    .setVideo(shareVideo)
                    .build();
            getShareDiaLog().show(shareVideoContent, ShareDialog.Mode.AUTOMATIC);
        }

    }

    @Override
    protected void initActionBar() {
        super.initActionBar();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().hide();
        }
    }

    private void initViewPager() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        mAdapter = new MediaAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(mCurrentPosition);
        mCurrentFile = mFileHolders.get(mCurrentPosition).getMediaFile();
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                mCurrentFile = mFileHolders.get(position).getMediaFile();
                mAdapter.hideActiveMediaControllers();
            }
        });
    }

    public static class PhotoFragment extends Fragment {
        private static final String ARG_PHOTO_FILE = "photo";
        private File mFile;
        private SupportPhotoView mPhotoImageView;
        private ActionBar mActionBar;

        public static Fragment newInstance(File mediaFile) {
            PhotoFragment photoFragment = new PhotoFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_PHOTO_FILE, mediaFile);
            photoFragment.setArguments(args);
            return photoFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            mActionBar = activity.getSupportActionBar();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
            mFile = (File) getArguments().getSerializable(ARG_PHOTO_FILE);
            mPhotoImageView = rootView.findViewById(R.id.photo_image_view);
            mPhotoImageView.setHostFragment(this);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            displayPhoto();
        }

        private void displayPhoto() {
            Glide.with(this)
                    .load(Uri.fromFile(mFile))
                    .into(mPhotoImageView);
        }

        @Override
        public void onPause() {
            mActionBar.hide();
            super.onPause();
        }
    }

    public static class VideoFragment extends Fragment {
        private static final String ARG_VIDEO_FILE = "video";
        private static final float MEDIA_CONTROLLER_ALPHA = 0.7f;
        private static final String TAG = "VideoFragment";

        private File mFile;
        private RelativeLayout mThumbnailLayout;
        private ImageView mThumbnailImageView;
        private VideoView mVideoView;
        private MediaController mMediaController;
        private ActionBarShowHideAnimator mAnimator;

        public static Fragment newInstance(File mediaFile) {
            VideoFragment VideoFragment = new VideoFragment();
            Bundle args = new Bundle();
            args.putSerializable(ARG_VIDEO_FILE, mediaFile);
            VideoFragment.setArguments(args);
            return VideoFragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
            setHasOptionsMenu(true);
            mFile = (File) getArguments().getSerializable(ARG_VIDEO_FILE);
            mAnimator = new ActionBarShowHideAnimator((AppCompatActivity) getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_video, container, false);
            mVideoView = rootView.findViewById(R.id.video_view);
            mThumbnailLayout = rootView.findViewById(R.id.thumbnail_layout);
            mThumbnailImageView = rootView.findViewById(R.id.thumbnail_image_view);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            initThumbnail();
            initVideoView();
        }

        private void initThumbnail() {
            Glide.with(getActivity())
                    .load(Uri.fromFile(mFile))
                    .into(mThumbnailImageView);
            mThumbnailLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mThumbnailLayout.setVisibility(View.GONE);
                    mVideoView.start();
                    animateShowHideActionBar();
                    mMediaController.show();
                }
            });
        }

        private void initVideoView() {
            mMediaController = new MediaController(getActivity());
            mMediaController.setBackgroundResource(android.R.color.transparent);
            mMediaController.setAlpha(MEDIA_CONTROLLER_ALPHA);
            mVideoView.setMediaController(mMediaController);

            mVideoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    animateShowHideActionBar();
                    mMediaController.show();
                    return true;
                }
            });

            mVideoView.setVideoURI(Uri.fromFile(mFile));
            mVideoView.pause();
        }

        private void animateShowHideActionBar() {
            mAnimator.animate();
        }

        private void hideMediaController() {
            mMediaController.hide();
        }
    }

    private class MediaAdapter extends FragmentStatePagerAdapter {
        private SparseArray<VideoFragment> mActiveVideoFragments;

        MediaAdapter(FragmentManager fm) {
            super(fm);
            mActiveVideoFragments = new SparseArray<>();
        }

        @Override
        public Fragment getItem(int position) {
            FileHolder fileholder = mFileHolders.get(position);
            File file = fileholder.getMediaFile();
            if (MediaUtils.checkSupportPhotoFile(file)) {
                return PhotoFragment.newInstance(file);
            } else if (MediaUtils.checkSupportVideoFile(file)) {
                return VideoFragment.newInstance(file);
            }
            return null;
        }

        @Override
        public int getCount() {
            return mFileHolders.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            if (isVideoFragment(position)) {
                VideoFragment VideoFragment = (VideoFragment) fragment;
                mActiveVideoFragments.put(position, VideoFragment);
            }
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (isVideoFragment(position)) {
                VideoFragment VideoFragment = (VideoFragment) object;
                int index = mActiveVideoFragments.indexOfValue(VideoFragment);
                mActiveVideoFragments.removeAt(index);
            }
            super.destroyItem(container, position, object);
        }

        private boolean isVideoFragment(int position) {
            return MediaUtils.checkSupportVideoFile(mFileHolders.get(position).getMediaFile());
        }

        private void hideActiveMediaControllers() {
            if (mActiveVideoFragments.size() != 0) {
                int count = mActiveVideoFragments.size();
                for (int i = 0; i < count; i++) {
                    int key = mActiveVideoFragments.keyAt(i);
                    VideoFragment VideoFragment = mActiveVideoFragments.get(key);
                    VideoFragment.hideMediaController();
                }
            }
        }
    }


}
