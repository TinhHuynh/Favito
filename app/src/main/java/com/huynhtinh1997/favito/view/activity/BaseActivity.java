package com.huynhtinh1997.favito.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.share.widget.ShareDialog;
import com.huynhtinh1997.favito.R;
import com.huynhtinh1997.favito.preferences.SharedPreference;
import com.huynhtinh1997.favito.view.dialog.ReadMeDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

/**
 * Created by huynhtinh1997 on 01/07/2017.
 */

public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";
    private static final String LOG_OUT = "out";
    private static final String LOG_IN = "in";

    private LoginManager mLoginManager;
    private CallbackManager mCallbackManager;
    private Menu mOptionMenu;
    private ShareDialog mShareDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFacebookLogin();
        initShareDiagLog();
    }

    protected void initActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null && !(this instanceof MainActivity)) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

                        fetchAndSaveProfileName(loginResult.getAccessToken());
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

    private void fetchAndSaveProfileName(AccessToken accessToken) {
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

    protected void saveProfileName(String profileName) {
        SharedPreference.saveProfileName(this, profileName);
    }

    protected String getSavedProfileName() {
        return SharedPreference.getSavedProfileName(this);
    }

    protected CallbackManager getCallbackManager() {
        return mCallbackManager;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    protected void initShareDiagLog() {
        mShareDialog = new ShareDialog(this);
    }

    protected ShareDialog getShareDiaLog() {
        return mShareDialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mOptionMenu = menu;

        initLogInMenuItem(menu);

        showNeccessaryMenuItems();

        return true;
    }

    protected Menu getOptionMenu() {
        return mOptionMenu;
    }

    private void initLogInMenuItem(Menu menu) {
        MenuItem logInOutItem = menu.findItem(R.id.login_logout);
        String temp = getString(R.string.log_in_out, isUserLogin() ? LOG_OUT : LOG_IN);
        logInOutItem.setTitle(temp);
    }

    protected abstract void showNeccessaryMenuItems();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.login_logout:
                if (isUserLogin()) {
                    logOut();
                } else {
                    logIn();
                }
                invalidateOptionsMenu();
                return true;

            case R.id.read_me:
                new ReadMeDialog(this).show();
                return true;

            case R.id.share_to_fb:
                shareMediaToFacebook();
                return true;

            case android.R.id.home:
                onBackPressed();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    protected void shareMediaToFacebook() {
        if (!isUserLogin()) {
            logIn();
            invalidateOptionsMenu();
        }
    }

    protected boolean isUserLogin() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && !accessToken.isExpired();
    }

    protected void logOut() {
        mLoginManager.logOut();
    }

    protected void logIn() {
        List<String> permissions = Arrays.asList("public_profile", "email");
        mLoginManager.logInWithReadPermissions(
                this,
                permissions
        );
    }

    protected void showToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
