package com.huynhtinh1997.favito.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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

import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareMediaContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.widget.ShareDialog;
import com.huynhtinh1997.favito.R;
import com.huynhtinh1997.favito.dialog.MediaDetailsDialog;
import com.huynhtinh1997.favito.model.FileHolder;
import com.huynhtinh1997.favito.utils.MediaUtils;
import com.huynhtinh1997.favito.utils.ScreenUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private final static int SPAN_COUNT = 3;
    private static final int MAX_NUM_OF_FILES_SHARED_TO_FB = 6;

    private RecyclerView mRecyclerView;
    private ArrayList<FileHolder> mFileHolders;
    private ThumbnailAdapter mMediaAdapter;
    private File mCurrentFile;
    private ArrayList<FileHolder> mSelectedFileHolders;
    private ShareDialog mShareDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSelectedFileHolders = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, SPAN_COUNT));

        loadFileHoldersList();

        if (isUserLogin()) {
            showToast("Welcome back, " + getSavedProfileName() + "!");
        }

        initShareDiagLog();

    }

    @Override
    protected void showNeccessaryMenuItems() {
        Menu menu = getOptionMenu();
        menu.findItem(R.id.photo_capture).setVisible(true);
        menu.findItem(R.id.video_record).setVisible(true);
        menu.findItem(R.id.share_to_fb).setVisible(!isListOfSelectedEmpty());
        menu.findItem(R.id.scan).setVisible(true);
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
                loadFileHoldersList();
                return true;
            case R.id.share_to_fb:
                shareMediaToFacebook();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void shareMediaToFacebook() {
        if (!isUserLogin()) {
            logIn();
            invalidateOptionsMenu();
        }

        ShareMediaContent.Builder builder = new ShareMediaContent.Builder();
        for (FileHolder fileHolder : mSelectedFileHolders) {
            File temp = fileHolder.getMediaFile();
            if (MediaUtils.checkSupportPhotoFile(temp)) {
                Bitmap photo = BitmapFactory.decodeFile(temp.getAbsolutePath());
                SharePhoto sharePhoto = new SharePhoto.Builder()
                        .setBitmap(photo)
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
        mShareDialog.show(shareMediaContent, ShareDialog.Mode.AUTOMATIC);

    }

    private void loadFileHoldersList() {
        new loadMediaListTask().execute();
    }

    private void updateUI() {
        Collections.sort(mFileHolders);
        if (mMediaAdapter == null) {
            mMediaAdapter = new ThumbnailAdapter(mFileHolders);
            mRecyclerView.setAdapter(mMediaAdapter);
        } else {
            mMediaAdapter.setFileHolders(mFileHolders);
            mMediaAdapter.notifyDataSetChanged();
        }

    }

    private void initShareDiagLog() {
        mShareDialog = new ShareDialog(this);
        mShareDialog.registerCallback(getCallbackManager(), new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
                if (isNumOfSelectedExceed()) {
                    showToast("Maximum number of total selected photos/videos:" + MAX_NUM_OF_FILES_SHARED_TO_FB);
                }
            }
        });

    }

    private boolean isNumOfSelectedExceed() {
        return mSelectedFileHolders.size() > 6;
    }

    private void dispatchCameraIntent(int requestCode) {
        String action = requestCode == REQUEST_IMAGE_CAPTURE ?
                MediaStore.ACTION_IMAGE_CAPTURE : MediaStore.ACTION_VIDEO_CAPTURE;
        Intent cameraIntent = new Intent(action);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                mCurrentFile = MediaUtils.createFile(requestCode == REQUEST_IMAGE_CAPTURE ?
                        MediaUtils.PHOTO : MediaUtils.VIDEO);
                Uri photoURI = Uri.fromFile(mCurrentFile);
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
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE) {
                MediaUtils.addFileToGallery(this, mCurrentFile);
                addCapturedMediaFileHolderToAdapter();
            }
            // when user cancels photo/ video from camera
        } else if ((requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE)) {
            mCurrentFile.delete();
        }
    }

    private void addCapturedMediaFileHolderToAdapter() {
        FileHolder fileHolder = new FileHolder();
        fileHolder.setMediaFile(mCurrentFile);
        mMediaAdapter.getFileHolders().add(0, fileHolder);
        mMediaAdapter.notifyItemInserted(0);
    }

    private void addFileHolderToList(FileHolder fileHolder) {
        mSelectedFileHolders.add(fileHolder);
    }

    private void removeFileHolder(FileHolder fileHolder) {
        mSelectedFileHolders.remove(fileHolder);
        invalidateOptionsMenu();
    }

    private void showNumOfSelectedThumbnailOnActionBar() {

        if (!isListOfSelectedEmpty()) {
            String subtitle = getString(R.string.num_of_selected_thumbnails, mSelectedFileHolders.size());
            getSupportActionBar().setSubtitle(subtitle);
        } else {
            getSupportActionBar().setSubtitle(null);
        }

    }

    private boolean isListOfSelectedEmpty() {
        return mSelectedFileHolders.isEmpty();
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

        private ThumbnailAdapter(ArrayList<FileHolder> paths) {
            this.mFileHolders = paths;
        }

        public ArrayList<FileHolder> getFileHolders() {
            return mFileHolders;
        }

        public void setFileHolders(ArrayList<FileHolder> fileHolders) {
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
        public void onBindViewHolder(ThumbnailViewHolder holder, int position) {
            FileHolder fileHolder = mFileHolders.get(position);
            holder.bindView(fileHolder);
        }

        @Override
        public int getItemCount() {
            return mFileHolders.size();
        }
    }

    private class ThumbnailViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private static final int SELECTED_ALPHA = 255 / 2;
        private static final int UNSELECTED = 255;

        private ImageView mThumbnailImageView;
        private ImageView mVideoImageView;
        private ImageButton mDetailsButton;
        private FrameLayout mLayout;
        private boolean mSelected;
        private FileHolder mFileHolder;

        private ThumbnailViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            mThumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            mVideoImageView = itemView.findViewById(R.id.video_image_view);
            mLayout = itemView.findViewById(R.id.list_item_layout);
            mDetailsButton = itemView.findViewById(R.id.detail_image_button);
            mSelected = false;
        }

        private void bindView(FileHolder fileHolder) {
            mFileHolder = fileHolder;
            Log.d(TAG, "ThumbnailViewHolder-bindView: file path: " + fileHolder.getFilePath());
            setThumbnailImageViewSize();
            MediaUtils.loadThumbnail(MainActivity.this, fileHolder.getMediaFile(), mThumbnailImageView);
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
            mDetailsButton.setVisibility(View.GONE);
        }

        private void setThumbnailImageViewSize() {
            int size = (int) (ScreenUtils.getScreenWidthByPixels(MainActivity.this) / 3);
            mThumbnailImageView.getLayoutParams().width = size;
            mThumbnailImageView.getLayoutParams().height = size;
            mThumbnailImageView.requestLayout();
        }

        @Override
        public void onClick(View view) {
            if (isSelected() || (!isSelected() && !isListOfSelectedEmpty())) {
                this.onLongClick(view);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (!isSelected()) {
                this.setSelectedUI();
                addFileHolderToList(mFileHolder);
                invalidateOptionsMenu();
            } else {
                this.setUnselectedUI();
                removeFileHolder(mFileHolder);
            }
            showNumOfSelectedThumbnailOnActionBar();
            mSelected = !mSelected;
            return true;
        }

        private boolean isSelected() {
            return mSelected;
        }

        private void setSelectedUI() {
            mThumbnailImageView.setImageAlpha(SELECTED_ALPHA);
            mLayout.setBackgroundResource(R.drawable.selected_border);
            mDetailsButton.setVisibility(View.VISIBLE);
        }

        private void setUnselectedUI() {
            mThumbnailImageView.setImageAlpha(UNSELECTED);
            mLayout.setBackgroundResource(R.drawable.unselected_border);
            mDetailsButton.setVisibility(View.GONE);
        }

    }


}
