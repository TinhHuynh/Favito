package com.huynhtinh1997.favito.view.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.widget.ShareDialog;
import com.huynhtinh1997.favito.R;
import com.huynhtinh1997.favito.model.FileHolder;
import com.huynhtinh1997.favito.utils.MediaUtils;
import com.huynhtinh1997.favito.utils.ScreenUtils;
import com.huynhtinh1997.favito.view.dialog.MediaDetailsDialog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private final static int SPAN_COUNT = 3;
    private static final int MAX_NUM_OF_FILES_SHARED_TO_FB = 6;

    private RecyclerView mRecyclerView;
    private ArrayList<FileHolder> mFileHolders;
    private ThumbnailAdapter mMediaAdapter;
    private File mCurrentCapturedFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initActionBar();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));

        loadListAndUpdateUI();

        if (isUserLogin()) {
            showToast("Welcome back, " + getSavedProfileName() + "!");
        }
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
                if (isNumOfSelectedExceed()) {
                    showToast("Maximum number of total photos/videos to upload: " + MAX_NUM_OF_FILES_SHARED_TO_FB);
                }
            }
        });
    }

    private boolean isNumOfSelectedExceed() {
        return mMediaAdapter.getSelectedFileHolders().size() > MAX_NUM_OF_FILES_SHARED_TO_FB;
    }

    @Override
    protected void showNeccessaryMenuItems() {
        Menu menu = getOptionMenu();
        menu.findItem(R.id.photo_capture).setVisible(true);
        menu.findItem(R.id.video_record).setVisible(true);
        menu.findItem(R.id.share_to_fb).setVisible(!isListOfSelectedEmpty());
        menu.findItem(R.id.scan).setVisible(true);
        menu.findItem(R.id.deselect).setVisible(!isListOfSelectedEmpty());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.photo_capture:
                dispatchCameraIntent(REQUEST_IMAGE_CAPTURE);
                return true;

            case R.id.video_record:
                dispatchCameraIntent(REQUEST_VIDEO_CAPTURE);
                return true;

            case R.id.scan:
                loadListAndUpdateUI();
                return true;

            case R.id.deselect:
                mMediaAdapter.deselectThumbnails();
                mMediaAdapter.removeAllSelectedFileHoldersAndPositions();
                updateActionBar();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void dispatchCameraIntent(int requestCode) {
        String action = requestCode == REQUEST_IMAGE_CAPTURE ?
                MediaStore.ACTION_IMAGE_CAPTURE : MediaStore.ACTION_VIDEO_CAPTURE;
        Intent cameraIntent = new Intent(action);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                mCurrentCapturedFile = MediaUtils.createFile(requestCode == REQUEST_IMAGE_CAPTURE ?
                        MediaUtils.PHOTO : MediaUtils.VIDEO);

                Uri photoURI = Uri.fromFile(mCurrentCapturedFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, requestCode);
            } catch (IOException ex) {
                Log.e(TAG, "Error while creating file", ex);
            }
        } else {
            showToast("I cannot find any camera app in your phone.\nPlease install one.");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (userAcceptResult(resultCode, requestCode)) {
            MediaUtils.addFileToGallery(this, mCurrentCapturedFile);
            addCapturedMediaFileHolderToAdapter();

        } else if (userCancelResult(resultCode, requestCode)) {
            mCurrentCapturedFile.delete();
        }
    }

    private boolean userAcceptResult(int resultCode, int requestCode) {
        return resultCode == RESULT_OK
                && (requestCode == REQUEST_IMAGE_CAPTURE
                || requestCode == REQUEST_VIDEO_CAPTURE);
    }

    private boolean userCancelResult(int resultCode, int requestCode) {
        return resultCode != RESULT_OK
                && (requestCode == REQUEST_IMAGE_CAPTURE
                || requestCode == REQUEST_VIDEO_CAPTURE);
    }

    private void addCapturedMediaFileHolderToAdapter() {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setMediaFile(mCurrentCapturedFile);
        mMediaAdapter.getFileHolders().add(0, fileHolder);
        mMediaAdapter.notifyItemInserted(0);
        mRecyclerView.smoothScrollToPosition(0);
        showToast("A photo/video has just been added");

    }

    private void loadListAndUpdateUI() {
        new loadMediaListTask().execute();
    }

    private void updateUI() {

        if (mMediaAdapter == null) {
            mMediaAdapter = new ThumbnailAdapter(mFileHolders);
            mRecyclerView.setAdapter(mMediaAdapter);
        } else {
            mMediaAdapter.setFileHolders(mFileHolders);
            mMediaAdapter.notifyDataSetChanged();
        }

        showToast("Found " + mFileHolders.size() + " photos/videos");
        // set visibility of deselect menu item
        invalidateOptionsMenu();
    }

    private void updateActionBar() {
        invalidateOptionsMenu();
        showNumOfSelectedThumbnailsOnActionBar();
    }

    private void showNumOfSelectedThumbnailsOnActionBar() {

        if (!isListOfSelectedEmpty()) {
            String subtitle = getString(R.string.num_of_selected_thumbnails,
                    mMediaAdapter.getSelectedFileHolders().size());
            getSupportActionBar().setSubtitle(subtitle);
        } else {
            getSupportActionBar().setSubtitle(null);
        }

    }

    private boolean isListOfSelectedEmpty() {
        return mMediaAdapter != null
                && mMediaAdapter.getSelectedFileHolders() != null
                && mMediaAdapter.getSelectedFileHolders().isEmpty();
    }

    @Override
    protected void shareMediaToFacebook() {
        super.shareMediaToFacebook();

        ShareMediaContent.Builder builder = new ShareMediaContent.Builder();

        for (FileHolder fileHolder : mMediaAdapter.getSelectedFileHolders()) {
            File temp = fileHolder.getMediaFile();

            if (MediaUtils.checkSupportPhotoFile(temp)) {
                SharePhoto sharePhoto = new SharePhoto.Builder()
                        .setImageUrl(Uri.fromFile(temp))
                        .build();
                builder.addMedium(sharePhoto);
            } else if (MediaUtils.checkSupportVideoFile(temp)) {
                ShareVideo shareVideo = new ShareVideo.Builder()
                        .setLocalUrl(Uri.fromFile(temp))
                        .build();
                builder.addMedium(shareVideo);
            }
        }

        ShareMediaContent shareMediaContent = builder.build();
        getShareDiaLog().show(shareMediaContent, ShareDialog.Mode.AUTOMATIC);

    }

    private class loadMediaListTask extends AsyncTask<Void, Void, ArrayList<FileHolder>> {
        ProgressBar mProgressBar;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar = (ProgressBar) findViewById(R.id.load_list_progress_bar);
            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<FileHolder> doInBackground(Void... voids) {
            return (ArrayList<FileHolder>) MediaUtils.getFileHolders();
        }

        @Override
        protected void onPostExecute(ArrayList<FileHolder> fileHolders) {
            super.onPostExecute(fileHolders);
            mProgressBar.setVisibility(View.GONE);
            mFileHolders = fileHolders;
            updateUI();
        }
    }

    private class ThumbnailAdapter extends RecyclerView.Adapter<ThumbnailViewHolder> {
        private ArrayList<FileHolder> mFileHolders;
        private ArrayList<FileHolder> mSelectedFileHolders;
        private ArrayList<Integer> mSelectedPositions;

        private ThumbnailAdapter(ArrayList<FileHolder> fileHolders) {
            this.mFileHolders = fileHolders;
            mSelectedFileHolders = new ArrayList<>();
            mSelectedPositions = new ArrayList<>();
        }

        ArrayList<FileHolder> getSelectedFileHolders() {
            return mSelectedFileHolders;
        }

        ArrayList<FileHolder> getFileHolders() {
            return mFileHolders;
        }

        void setFileHolders(ArrayList<FileHolder> fileHolders) {
            mFileHolders = fileHolders;
        }

        @Override
        public ThumbnailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater
                    .from(MainActivity.this)
                    .inflate(R.layout.fragment_list_item, parent, false);
            return new ThumbnailViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ThumbnailViewHolder viewHolder, int position) {
            FileHolder fileHolder = mFileHolders.get(position);

            viewHolder.bindView(fileHolder);
        }

        @Override
        public int getItemCount() {
            return mFileHolders.size();
        }

        private void addSelectedFileHolderAndPosition(FileHolder fileHolder, int position) {
            mSelectedFileHolders.add(fileHolder);
            mSelectedPositions.add(position);
        }

        private void removeSelectedFileHolderAndPosition(FileHolder fileHolder, int position) {
            mSelectedFileHolders.remove(fileHolder);
            mSelectedPositions.remove(Integer.valueOf(position));
        }

        private void deselectThumbnails() {
            for (int position : mSelectedPositions) {
                ThumbnailViewHolder viewHolder =
                        (ThumbnailViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);
                if (viewHolder != null) {
                    viewHolder.setUnselectedUI();
                }
            }
        }

        public void removeAllSelectedFileHoldersAndPositions() {
            mSelectedFileHolders.clear();
            mSelectedPositions.clear();
        }

        private boolean isMultiSelectActive() {
            return !isListOfSelectedEmpty();
        }
    }

    private class ThumbnailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {
        private static final int SELECTED_ALPHA = 255 / 2;
        private static final int UNSELECTED_ALPHA = 255;

        private ImageView mThumbnailImageView;
        private ImageView mVideoImageView;
        private ImageButton mDetailsButton;
        private FrameLayout mLayout;
        private FileHolder mFileHolder;

        private ThumbnailViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mThumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            mVideoImageView = itemView.findViewById(R.id.video_image_view);
            mLayout = itemView.findViewById(R.id.list_item_layout);
            mDetailsButton = itemView.findViewById(R.id.detail_image_button);
        }

        private void bindView(FileHolder fileHolder) {
            mFileHolder = fileHolder;
            updateThumbnailImageView();
            mVideoImageView.setVisibility(View.GONE);
            if (MediaUtils.checkSupportVideoFile(mFileHolder.getMediaFile())) {
                mVideoImageView.setVisibility(View.VISIBLE);
            }
            mDetailsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new MediaDetailsDialog(MainActivity.this, mFileHolder.getMediaFile()).show();
                }
            });
            if (isSelected()) {
                setSelectedUI();
            } else {
                setUnselectedUI();
            }
        }

        private void updateThumbnailImageView() {
            int size = (int) (ScreenUtils.getScreenWidthByPixels(MainActivity.this) / 3);
            mThumbnailImageView.getLayoutParams().width = size;
            mThumbnailImageView.getLayoutParams().height = size;
            mThumbnailImageView.requestLayout();

            Glide.with(MainActivity.this)
                    .load(Uri.fromFile(mFileHolder.getMediaFile()))
                    .into(mThumbnailImageView);
        }

        @Override
        public void onClick(View view) {
            if (!isSelected() && !mMediaAdapter.isMultiSelectActive()) {
                startActivity(MediaActivity.newIntent(MainActivity.this, getAdapterPosition(), mFileHolders));
            } else if (isSelected() || (!isSelected() && mMediaAdapter.isMultiSelectActive())) {
                updateUIAndSelectedList(view);
            }
        }

        private void updateUIAndSelectedList(View view) {
            onLongClick(view);
        }

        @Override
        public boolean onLongClick(View view) {
            if (!isSelected()) {
                this.setSelectedUI();
                mMediaAdapter.addSelectedFileHolderAndPosition(mFileHolder, getAdapterPosition());
            } else {
                this.setUnselectedUI();
                mMediaAdapter.removeSelectedFileHolderAndPosition(mFileHolder, getAdapterPosition());
            }
            updateActionBar();
            return true;
        }

        private boolean isSelected() {
            return mMediaAdapter.getSelectedFileHolders().contains(mFileHolder);
        }

        private void setSelectedUI() {
            mThumbnailImageView.setImageAlpha(SELECTED_ALPHA);
            mLayout.setBackgroundResource(R.drawable.selected_border);
            mDetailsButton.setVisibility(View.VISIBLE);
        }

        private void setUnselectedUI() {
            mThumbnailImageView.setImageAlpha(UNSELECTED_ALPHA);
            mLayout.setBackgroundResource(R.drawable.unselected_border);
            mDetailsButton.setVisibility(View.GONE);
        }

    }

}
