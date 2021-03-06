package org.bittweet.android.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.crashlytics.android.Crashlytics;

import org.bittweet.android.BuildConfig;
import org.bittweet.android.R;
import org.bittweet.android.internal.MyTwitterFactory;
import org.bittweet.android.ui.fragments.HomeTweetsListFragment;
import org.bittweet.android.ui.fragments.MentionsTweetsListFragment;
import org.bittweet.android.ui.fragments.TweetsDetailFragment;
import org.bittweet.android.ui.fragments.TweetsListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TweetsListActivity extends FragmentActivity implements TweetsListFragment.Callbacks {
    private boolean isTwoPane;

    private SharedPreferences twitPref;
    private FragmentManager fragmentManager;

    //private static final String TAG = "TweetsListActivity";
    public static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private ListView drawerList;

    private Activity activity;

    public boolean isTwitterLoggedInAlready() {
        return twitPref.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!BuildConfig.DEBUG) {
            Crashlytics.start(this);
        }

        activity = TweetsListActivity.this;
        twitPref = getSharedPreferences("MyTwitter", MODE_PRIVATE);
        fragmentManager = getSupportFragmentManager();

        if (!isTwitterLoggedInAlready()) {
            startActivity(new Intent(getApplicationContext(), TwitterLoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_tweets_list);
        initializeDrawer();

        if (findViewById(R.id.tweets_detail_container) != null) {
            isTwoPane = true;
        }

        /*IntentFilter filter = new IntentFilter();
        filter.addAction("org.bittweet.android.services.TweetService");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(tweetReceiver, filter);*/

        loadHomeTimeline();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getBooleanExtra("LOGOUT", false)) {
            System.err.println("Got LOGOUT. Logging out.");
            finish();
            Intent logout = new Intent(TweetsListActivity.this, TwitterLoginActivity.class);
            startActivity(logout);
        } else if (intent.getBooleanExtra("RESTART", false)) {
            System.err.println("Got RESTART. Logging out.");
            recreate();
        }
    }

    private void initializeDrawer() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerList = (ListView) findViewById(R.id.left_drawer);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.drawable.bittweet_ic_navigation_drawer, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }
        };

        drawerLayout.setDrawerListener(drawerToggle);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        String[] items = getResources().getStringArray(R.array.nav_drawer_items);
        TypedArray icons = getResources().obtainTypedArray(R.array.nav_drawer_icons);
        List<Map<String, String>> navItems = new ArrayList<Map<String, String>>();
        Map<String, String> navItemTemp;

        for(int i = 0; i < items.length; i++) {
            navItemTemp = new HashMap<String, String>();
            navItemTemp.put("icon", String.valueOf(icons.getResourceId(i, -1)));
            navItemTemp.put("text", items[i]);
            navItems.add(navItemTemp);
        }

        drawerList.setAdapter(new SimpleAdapter(this, navItems, R.layout.navigation_item, new String[] { "icon", "text" }, new int[] { R.id.nav_icon, R.id.nav_text }));

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        loadHomeTimeline();
                        break;
                    case 1:
                        loadMentionsTimeline();
                        break;
                    case 2:
                        Intent profIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                        profIntent.putExtra("USERNAME", twitPref.getString("USERNAME", ""));
                        startActivity(profIntent);
                        break;
                    case 3:
                        Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(settingsIntent);
                        break;
                }

                drawerLayout.closeDrawer(drawerList);
            }
        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        icons.recycle();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction("org.bittweet.android.services.TweetService");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(tweetReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        Crouton.cancelAllCroutons();
        try {
            this.unregisterReceiver(tweetReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemSelected(String id) {
        System.out.println("Item was selected");
        if (isTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(TweetsDetailFragment.ARG_ITEM_ID, id);

            TweetsDetailFragment fragment = new TweetsDetailFragment();
            fragment.setArguments(arguments);

            fragmentManager.beginTransaction()
                    .replace(R.id.tweets_detail_container, fragment)
                    .commit();
        } else {
            Intent detailIntent = new Intent(this, TweetsDetailActivity.class);
            detailIntent.putExtra(TweetsDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tweet_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_tweet:
                Intent newTweet = new Intent(getApplicationContext(), NewTweetActivity.class);
                startActivity(newTweet);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadMentionsTimeline() {
        MentionsTweetsListFragment fragment;

        try {
            fragment = MentionsTweetsListFragment.class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.tweets_list_container, fragment)
                .commit();


        fragment.setActivateOnItemClick(isTwoPane);
        getActionBar().setSubtitle(R.string.mentions_timeline);
    }

    private void loadHomeTimeline() {
        HomeTweetsListFragment fragment;

        try {
            fragment = HomeTweetsListFragment.class.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        fragmentManager.beginTransaction()
                .replace(R.id.tweets_list_container, fragment)
                .commit();

        fragment.setActivateOnItemClick(isTwoPane);
        getActionBar().setSubtitle(R.string.home_timeline);
    }

    private BroadcastReceiver tweetReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.err.println("Broadcast received!");
            String status = twitPref.getString("TWEET_SEND", "null");
            System.err.println(status);
            if (status.equals("sending")) {
                Crouton.showText(activity, R.string.tweet_updating, Style.INFO);
            } else if (status.equals("sent")) {
                Crouton.showText(activity, R.string.tweet_sent, Style.CONFIRM);
            } else if (status.equals("error")) {
                Crouton.showText(activity, R.string.tweet_error, Style.ALERT);
            }

            twitPref.edit().putString("TWEET_SEND", "null").apply();
        }
    };
}
