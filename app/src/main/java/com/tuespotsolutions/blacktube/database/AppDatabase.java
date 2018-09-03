package com.tuespotsolutions.blacktube.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

import com.tuespotsolutions.blacktube.database.history.Converters;
import com.tuespotsolutions.blacktube.database.history.dao.SearchHistoryDAO;
import com.tuespotsolutions.blacktube.database.history.dao.WatchHistoryDAO;
import com.tuespotsolutions.blacktube.database.history.model.SearchHistoryEntry;
import com.tuespotsolutions.blacktube.database.history.model.WatchHistoryEntry;
import com.tuespotsolutions.blacktube.database.subscription.SubscriptionDAO;
import com.tuespotsolutions.blacktube.database.subscription.SubscriptionEntity;

@TypeConverters({Converters.class})
@Database(entities = {SubscriptionEntity.class, WatchHistoryEntry.class, SearchHistoryEntry.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public static final String DATABASE_NAME = "newpipe.db";

    public abstract SubscriptionDAO subscriptionDAO();

    public abstract WatchHistoryDAO watchHistoryDAO();

    public abstract SearchHistoryDAO searchHistoryDAO();
}
