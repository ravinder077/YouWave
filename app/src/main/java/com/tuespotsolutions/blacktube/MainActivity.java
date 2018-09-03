/*
 * Created by Christian Schabesberger on 02.08.16.
 * <p>
 * Copyright (C) Christian Schabesberger 2016 <chris.schabesberger@mailbox.org>
 * DownloadActivity.java is part of NewPipe.
 * <p>
 * NewPipe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * NewPipe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with NewPipe.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tuespotsolutions.blacktube;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import com.tuespotsolutions.blacktube.database.AppDatabase;
import com.tuespotsolutions.blacktube.database.history.dao.HistoryDAO;
import com.tuespotsolutions.blacktube.database.history.dao.SearchHistoryDAO;
import com.tuespotsolutions.blacktube.database.history.dao.WatchHistoryDAO;
import com.tuespotsolutions.blacktube.database.history.model.HistoryEntry;
import com.tuespotsolutions.blacktube.database.history.model.SearchHistoryEntry;
import com.tuespotsolutions.blacktube.database.history.model.WatchHistoryEntry;
import extractor.StreamingService;
import extractor.stream.AudioStream;
import extractor.stream.StreamInfo;
import extractor.stream.VideoStream;
import com.tuespotsolutions.blacktube.fragments.BackPressable;
import com.tuespotsolutions.blacktube.fragments.detail.VideoDetailFragment;
import com.tuespotsolutions.blacktube.fragments.list.search.SearchFragment;
import com.tuespotsolutions.blacktube.history.HistoryListener;
import com.tuespotsolutions.blacktube.util.Constants;
import com.tuespotsolutions.blacktube.util.NavigationHelper;
import com.tuespotsolutions.blacktube.util.StateSaver;
import com.tuespotsolutions.blacktube.util.ThemeHelper;

import java.util.Date;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements HistoryListener {
    private static final String TAG = "MainActivity";
    public static final boolean DEBUG = false;
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;
    private LinearLayout rel;
    private SwipeRefreshLayout pullToRefresh;
    public static final String inter="ca-app-pub-3940256099942544/1033173712";
    public static final String banner="ca-app-pub-3940256099942544/6300978111";
    public static final String interplay="ca-app-pub-3940256099942544/1033173712";
    public static final String bannerplay="ca-app-pub-3940256099942544/6300978111";
    public static final AdRequest adRequest = new AdRequest.Builder().build();
    public static InterstitialAd interstitialAd1;
    public static com.google.android.gms.ads.AdView b1;
    /*//////////////////////////////////////////////////////////////////////////
    // Activity's LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        ThemeHelper.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, getString(R.string.mob_init));
        if (getSupportFragmentManager() != null && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            initFragments();
        }

        /**********************ids********************/
       // pullToRefresh = findViewById(R.id.pullToRefresh);
        rel = findViewById(R.id.banner);
        /**********************ids********************/


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


  /*      pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recreate();
            }
        });*/
        interstitialAd1 = new InterstitialAd(this);
        interstitialAd1.setAdUnitId(inter);
        interstitialAd1.loadAd(adRequest);

        /*********************c_banner*********************/
        b1 = new AdView(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        b1.setLayoutParams(lp);
        b1.setAdSize(AdSize.SMART_BANNER);
        b1.setAdUnitId(banner);
/*********************c_banner*********************/
        rel.addView(b1);
        /*****************c_banner********************/
        b1.loadAd(adRequest);
        b1.setAdListener(new AdListener(){
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                //Toast.makeText(MainActivity.this, "b1 Loaded", Toast.LENGTH_SHORT).show();
                System.err.println("c_ban1 Loaded");
            }
            @Override
            public void onAdClosed() {
                //Toast.makeText(MainActivity.this, "Close b1", Toast.LENGTH_SHORT).show();
                System.err.println("Close ad c_ban1");
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.

                //Toast.makeText(MainActivity.this, "Failed to load b1", Toast.LENGTH_SHORT).show();
                System.err.println("Failed to Load c_ban1");
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Toast.makeText(MainActivity.this, "Wait...", Toast.LENGTH_SHORT).show();
                System.err.println("ad Opened c_ban1");
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.

                Toast.makeText(MainActivity.this, "Wait...", Toast.LENGTH_SHORT).show();
                System.err.println("ad Left Application c_ban1");
                //Toast.makeText(MainActivity.this, "Timestamp=="+timeStamp, Toast.LENGTH_SHORT).show();
            }
        });
        /*****************c_banner********************/


        initHistory();
    }
public static void banner()
{

}
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            StateSaver.clearStateFiles();
        }

        disposeHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean(Constants.KEY_THEME_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "Theme has changed, recreating activity...");
            sharedPreferences.edit().putBoolean(Constants.KEY_THEME_CHANGE, false).apply();
            // https://stackoverflow.com/questions/10844112/runtimeexception-performing-pause-of-activity-that-is-not-resumed
            // Briefly, let the activity resume properly posting the recreate call to end of the message queue
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.recreate();
                }
            });
        }

        if(sharedPreferences.getBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false)) {
            if (DEBUG) Log.d(TAG, "main page has changed, recreating main fragment...");
            sharedPreferences.edit().putBoolean(Constants.KEY_MAIN_PAGE_CHANGE, false).apply();
            NavigationHelper.openMainActivity(this);
        }

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "onNewIntent() called with: intent = [" + intent + "]");
        if (intent != null) {
            // Return if launched from a launcher (e.g. Nova Launcher, Pixel Launcher ...)
            // to not destroy the already created backstack
            String action = intent.getAction();
            if ((action != null && action.equals(Intent.ACTION_MAIN)) && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) return;
        }

        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onBackPressed() {
        if (DEBUG) Log.d(TAG, "onBackPressed() called");

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        // If current fragment implements BackPressable (i.e. can/wanna handle back press) delegate the back press to it
        if (fragment instanceof BackPressable) {
            if (((BackPressable) fragment).onBackPressed()) return;
        }


        if (getSupportFragmentManager().getBackStackEntryCount() == 1) {
                interstitialAd1.show();
                finish();
        } else super.onBackPressed();
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Menu
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "]");
        super.onCreateOptionsMenu(menu);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment_holder);
        if (!(fragment instanceof VideoDetailFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_spinner).setVisibility(View.GONE);
        }

        if (!(fragment instanceof SearchFragment)) {
            findViewById(R.id.toolbar).findViewById(R.id.toolbar_search_container).setVisibility(View.GONE);

            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.main_menu, menu);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (DEBUG) Log.d(TAG, "onOptionsItemSelected() called with: item = [" + item + "]");
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                NavigationHelper.gotoMainFragment(getSupportFragmentManager());
                return true;
            case R.id.action_settings:
                if(interstitialAd1.isLoaded()) {
                    interstitialAd1.show();
                    NavigationHelper.openSettings(this);
                }
                else {
                    NavigationHelper.openSettings(this);
                }
                return true;
            case R.id.action_show_downloads:
                if(interstitialAd1.isLoaded()) {
                    interstitialAd1.show();
                    NavigationHelper.openDownloads(this);
                }
                else {
                    NavigationHelper.openDownloads(this);
                }
                return true;
            case R.id.action_about:
                //NavigationHelper.openAbout(this);
                return true;
            case R.id.action_history:
                NavigationHelper.openHistory(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    private void initFragments() {
        if (DEBUG) Log.d(TAG, "initFragments() called");
        StateSaver.clearStateFiles();
        if (getIntent() != null && getIntent().hasExtra(Constants.KEY_LINK_TYPE)) {
            handleIntent(getIntent());
        } else NavigationHelper.gotoMainFragment(getSupportFragmentManager());
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    private void handleIntent(Intent intent) {
        if (DEBUG) Log.d(TAG, "handleIntent() called with: intent = [" + intent + "]");

        if (intent.hasExtra(Constants.KEY_LINK_TYPE)) {
            String url = intent.getStringExtra(Constants.KEY_URL);
            int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
            String title = intent.getStringExtra(Constants.KEY_TITLE);
            switch (((StreamingService.LinkType) intent.getSerializableExtra(Constants.KEY_LINK_TYPE))) {
                case STREAM:
                    boolean autoPlay = intent.getBooleanExtra(VideoDetailFragment.AUTO_PLAY, false);
                    NavigationHelper.openVideoDetailFragment(getSupportFragmentManager(), serviceId, url, title, autoPlay);
                    break;
                case CHANNEL:
                    NavigationHelper.openChannelFragment(getSupportFragmentManager(), serviceId, url, title);
                    break;
                case PLAYLIST:
                    NavigationHelper.openPlaylistFragment(getSupportFragmentManager(), serviceId, url, title);
                    break;
            }
        } else if (intent.hasExtra(Constants.KEY_OPEN_SEARCH)) {
            String searchQuery = intent.getStringExtra(Constants.KEY_QUERY);
            if (searchQuery == null) searchQuery = "";
            int serviceId = intent.getIntExtra(Constants.KEY_SERVICE_ID, 0);
            NavigationHelper.openSearchFragment(getSupportFragmentManager(), serviceId, searchQuery);
        } else {
            NavigationHelper.gotoMainFragment(getSupportFragmentManager());
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // History
    //////////////////////////////////////////////////////////////////////////*/

    private WatchHistoryDAO watchHistoryDAO;
    private SearchHistoryDAO searchHistoryDAO;
    private PublishSubject<HistoryEntry> historyEntrySubject;
    private Disposable disposable;

    private void initHistory() {
        final AppDatabase database = NewPipeDatabase.getInstance();
        watchHistoryDAO = database.watchHistoryDAO();
        searchHistoryDAO = database.searchHistoryDAO();
        historyEntrySubject = PublishSubject.create();
        disposable = historyEntrySubject
                .observeOn(Schedulers.io())
                .subscribe(getHistoryEntryConsumer());
    }

    private void disposeHistory() {
        if (disposable != null) disposable.dispose();
        watchHistoryDAO = null;
        searchHistoryDAO = null;
    }

    @NonNull
    private Consumer<HistoryEntry> getHistoryEntryConsumer() {
        return new Consumer<HistoryEntry>() {
            @Override
            public void accept(HistoryEntry historyEntry) throws Exception {
                //noinspection unchecked
                HistoryDAO<HistoryEntry> historyDAO = (HistoryDAO<HistoryEntry>)
                        (historyEntry instanceof SearchHistoryEntry ? searchHistoryDAO : watchHistoryDAO);

                HistoryEntry latestEntry = historyDAO.getLatestEntry();
                if (historyEntry.hasEqualValues(latestEntry)) {
                    latestEntry.setCreationDate(historyEntry.getCreationDate());
                    historyDAO.update(latestEntry);
                } else {
                    historyDAO.insert(historyEntry);
                }
            }
        };
    }

    private void addWatchHistoryEntry(StreamInfo streamInfo) {
        if (sharedPreferences.getBoolean(getString(R.string.enable_watch_history_key), true)) {
            WatchHistoryEntry entry = new WatchHistoryEntry(streamInfo);
            historyEntrySubject.onNext(entry);
        }
    }

    @Override
    public void onVideoPlayed(StreamInfo streamInfo, @Nullable VideoStream videoStream) {
        addWatchHistoryEntry(streamInfo);
    }

    @Override
    public void onAudioPlayed(StreamInfo streamInfo, AudioStream audioStream) {
        addWatchHistoryEntry(streamInfo);
    }

    @Override
    public void onSearch(int serviceId, String query) {
        // Add search history entry
        if (sharedPreferences.getBoolean(getString(R.string.enable_search_history_key), true)) {
            SearchHistoryEntry searchHistoryEntry = new SearchHistoryEntry(new Date(), serviceId, query);
            historyEntrySubject.onNext(searchHistoryEntry);
        }
    }
}
