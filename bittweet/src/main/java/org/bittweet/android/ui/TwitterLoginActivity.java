package org.bittweet.android.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.internal.SecretKeys;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterLoginActivity extends FragmentActivity {
    // Constants
    static String TWITTER_CONSUMER_KEY = SecretKeys.CONSUMER_KEY;
    static String TWITTER_CONSUMER_SECRET = SecretKeys.CONSUMER_SECRET;

    // Preference Constants
    static String PREFERENCE_NAME = "twitter_oauth";
    public static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    public static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    static final String TWITTER_CALLBACK_URL = "http://www.chromatiqa.org/bittweet/";

    // Twitter oauth urls
    static final String URL_TWITTER_AUTH = "auth_url";
    static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
    static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

    // Logging
    private static final String TAG = "TwitterLoginActivity";

    // Views
    private Button loginButton;
    private WebView webView;

    // Twitter
    private Twitter twitter;
    private RequestToken requestToken;

    // Misc
    private SharedPreferences sharedPreferences;
    private ImageView blurredOverlay;
    private ImageView background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getActionBar().hide();

        twitter = MyTwitterFactory.getInstance(this).getTwitter();
        loginButton = (Button) findViewById(R.id.button_login);
        webView = (WebView) findViewById(R.id.webview);
        sharedPreferences = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        background = (ImageView) findViewById(R.id.background);
        blurredOverlay = (ImageView) findViewById(R.id.blurback);

        Bitmap blurred = Blur.loadBitmapFromView(background);
        Blur.blurBitmap(blurred, background, blurredOverlay, this);

        // When user clicks login button, should open up a web view that allows them to log in
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActionBar().show();
                new RequestAuthTask().execute();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        System.err.println(intent.toString());

        Uri uri = intent.getData();
        System.err.println("Uri is " + uri);

        if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
            new HandleAuthTask(uri).execute();
        }
    }

    // AsyncTask that allows user to sign in to obtain API token
    private class RequestAuthTask extends AsyncTask<Void, String, String> {

        @Override
        protected String doInBackground(Void... args) {
            String authUrl = "";

            try {
                // Get the request token using the callback URL
                requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
                authUrl = requestToken.getAuthenticationURL();
            } catch (TwitterException e) {
                e.printStackTrace();
                Log.e(TAG, "Could not get authURL");
            }

            return authUrl;
        }

        @Override
        protected void onPostExecute(String result) {
            // If successful and the authURL was obtained, then start a web view that allows user to sign in.
            if (!result.equals("")) {
                displayLoginPage(result);
            } else {
                Toast.makeText(TwitterLoginActivity.this, "Could not OAuth. Restart the app completely.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLoginPage(String url) {
        loginButton.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = Uri.parse(url);
                intent.setData(uri);
                onNewIntent(intent);

                return super.shouldOverrideUrlLoading(view, url);
            }
        }); webView.loadUrl(url);
    }

    // AsyncTask that obtains the API access key and secret, and stores into SharedPreferences
    private class HandleAuthTask extends AsyncTask<Void, Void, Void> {
        private AccessToken accessToken = null;
        private Uri mUri;

        public HandleAuthTask(Uri uri) {
            accessToken = null;
            mUri = uri;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mUri != null && mUri.toString().startsWith(TWITTER_CALLBACK_URL)) {
                // oAuth verifier
                String verifier = mUri.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

                try {
                    // Get the access token
                    accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
                } catch (TwitterException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Could not get access token");
                    return null;
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    return null;
                }
            } else if (mUri == null) {
                Log.e(TAG, "Uri is null");
            } else {
                Log.e(TAG, mUri.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (accessToken == null) {
                // TODO: Display error
                Log.e(TAG, "Token is null");
                return;
            }

            twitter.setOAuthAccessToken(accessToken);

            /* Save credentials to preferences for future use */
            String token = accessToken.getToken();
            String secret = accessToken.getTokenSecret();

            // Shared Preferences
            SharedPreferences.Editor e = sharedPreferences.edit();

            // After getting access token, access token secret
            // store them in application preferences
            e.putString(PREF_KEY_OAUTH_TOKEN, token);
            e.putString(PREF_KEY_OAUTH_SECRET, secret);

            // Store login status - true
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.commit(); // save changes

            Log.e("Twitter OAuth Token", "> " + token);
            System.err.println("Oauth token is " + token);

            Toast.makeText(TwitterLoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(TwitterLoginActivity.this, TweetsListActivity.class));
            finish();
        }
    }
}
