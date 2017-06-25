package com.huynhtinh1997.favito;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_VIDEO_CAPTURE = 2;
    private static final String PREFIX_PHOTO_FILE_NAME = "JPEG_";
    private static final String PREFIX_VIDEO_FILE_NAME = "MP4_";
    private static final String DEFAULT_PHOTO_FILE_TYPE = ".jpg";
    private static final String DEFAULT_VIDEO_FILE_TYPE = ".mp4";
    private static final String LOG_IN = "in";
    private static final String LOG_OUT = "out";
    private static final String PREFERENCE_PROFILE_NAME = "profile name";
    private static final int MODE_PROFILE_NAME_PREFERENCE = 123;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mTabViewPager;
    private String mCurrentPhotoPath;

    private CallbackManager mCallbackManager;
    private LoginManager mLoginManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTabViewPager = (ViewPager) findViewById(R.id.tab_view_pager);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mTabViewPager.setAdapter(mSectionsPagerAdapter);

        initFacebookLogin();

        if (isUserLogin()) {
            showToast("Welcome back, " + getSavedProfileName() + "!");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem logInOutItem = menu.findItem(R.id.login_logout);
        String temp = getString(R.string.log_in_out, isUserLogin() ? LOG_OUT : LOG_IN);
        logInOutItem.setTitle(temp);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.photo_capture) {
            dispatchCameraIntent(REQUEST_IMAGE_CAPTURE);
            return true;
        } else if (id == R.id.video_record) {
            dispatchCameraIntent(REQUEST_VIDEO_CAPTURE);
            return true;
        } else if (id == R.id.login_logout) {
            if (isUserLogin()) {
                logOut();
            } else {
                logIn();
            }
            invalidateOptionsMenu();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initFacebookLogin() {
        mCallbackManager = CallbackManager.Factory.create();
        mLoginManager = LoginManager.getInstance();
        mLoginManager.registerCallback(
                mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(TAG, "initFacebookLogin:onSuccess "
                                + loginResult.getAccessToken().getUserId() + " - "
                                + loginResult.getAccessToken().getToken());
                        getProfileName(loginResult.getAccessToken());
                        showToast("Welcome " + getSavedProfileName() + "!");
                        invalidateOptionsMenu();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e(TAG, "initFacebookLogin:onError ", error);
                        showToast("Failed to log in");
                    }
                }
        );
    }


    private void getProfileName(AccessToken accessToken) {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(
                            JSONObject object,
                            GraphResponse response) {
                        if (object != null) {
                            try {
                                Log.d(TAG, "getProfileName: User JSON " + object.toString());
                                String profileName = object.getString("name");
                                saveProfileName(profileName);
                            } catch (JSONException e) {
                                Log.e(TAG, "getProfileName: failed to get profile name", e);
                            }
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void saveProfileName(String profileName) {
        getSharedPreferences(PREFERENCE_PROFILE_NAME, MODE_PROFILE_NAME_PREFERENCE)
                .edit()
                .putString(PREFERENCE_PROFILE_NAME, profileName)
                .apply();
    }

    private String getSavedProfileName() {
        return getSharedPreferences(PREFERENCE_PROFILE_NAME, MODE_PROFILE_NAME_PREFERENCE)
                .getString(PREFERENCE_PROFILE_NAME, null);

    }

    private boolean isUserLogin() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && !accessToken.isExpired();
    }

    private void logOut() {
        mLoginManager.logOut();
    }

    private void logIn() {
        List<String> permissions = Arrays.asList("public_profile", "email");
        mLoginManager.logInWithReadPermissions(
                this,
                permissions
        );

    }

    private void dispatchCameraIntent(int requestCode) {
        String action = requestCode == REQUEST_IMAGE_CAPTURE ?
                MediaStore.ACTION_IMAGE_CAPTURE : MediaStore.ACTION_VIDEO_CAPTURE;
        Intent cameraIntent = new Intent(action);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createFile(requestCode);
            } catch (IOException ex) {
                Log.e(TAG, "Error while creating file", ex);
            }
            if (photoFile != null) {
                Uri photoURI = Uri.fromFile(photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(cameraIntent, requestCode);
            }

        } else {
            showToast("I cannot find any camera app in your phone.\nPlease install one.");
        }
    }

    private File createFile(int requestCode) throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = (requestCode == REQUEST_IMAGE_CAPTURE ?
                PREFIX_PHOTO_FILE_NAME : PREFIX_VIDEO_FILE_NAME) + timeStamp + "_";

        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File favitoDir = new File(storageDir.getAbsolutePath() + "/Favito");
        favitoDir.mkdir();
        File image = File.createTempFile(
                imageFileName,
                requestCode == REQUEST_IMAGE_CAPTURE ? DEFAULT_PHOTO_FILE_TYPE : DEFAULT_VIDEO_FILE_TYPE,
                favitoDir
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d(TAG, "Current photo path:" + mCurrentPhotoPath);
        return image;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE || requestCode == REQUEST_VIDEO_CAPTURE) {
                addFileToGallery();
            }
        } else {
            File abandonedFile = new File(mCurrentPhotoPath);
            abandonedFile.delete();
        }

    }

    private void addFileToGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
    }

    private void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }


    public static class PlaceholderFragment extends Fragment {

        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            setRetainInstance(true);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}
