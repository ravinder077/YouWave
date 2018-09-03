package com.tuespotsolutions.blacktube.fragments.subscription;

import android.util.Log;

import com.tuespotsolutions.blacktube.MainActivity;
import com.tuespotsolutions.blacktube.NewPipeDatabase;
import com.tuespotsolutions.blacktube.database.AppDatabase;
import com.tuespotsolutions.blacktube.database.subscription.SubscriptionDAO;
import com.tuespotsolutions.blacktube.database.subscription.SubscriptionEntity;
import extractor.channel.ChannelInfo;
import com.tuespotsolutions.blacktube.util.ExtractorHelper;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Scheduler;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Subscription Service singleton:
 * Provides a basis for channel Subscriptions.
 * Provides access to subscription table in database as well as
 * up-to-date observations on the subscribed channels
 */
public class SubscriptionService {

    private static final SubscriptionService sInstance = new SubscriptionService();

    public static SubscriptionService getInstance() {
        return sInstance;
    }

    protected final String TAG = "SubscriptionService@" + Integer.toHexString(hashCode());
    protected static final boolean DEBUG = MainActivity.DEBUG;
    private static final int SUBSCRIPTION_DEBOUNCE_INTERVAL = 500;
    private static final int SUBSCRIPTION_THREAD_POOL_SIZE = 4;

    private AppDatabase db;
    private Flowable<List<SubscriptionEntity>> subscription;

    private Scheduler subscriptionScheduler;

    private SubscriptionService() {
        db = NewPipeDatabase.getInstance();
        subscription = getSubscriptionInfos();

        final Executor subscriptionExecutor = Executors.newFixedThreadPool(SUBSCRIPTION_THREAD_POOL_SIZE);
        subscriptionScheduler = Schedulers.from(subscriptionExecutor);
    }

    /**
     * Part of subscription observation pipeline
     *
     * @see SubscriptionService#getSubscription()
     */
    private Flowable<List<SubscriptionEntity>> getSubscriptionInfos() {
        return subscriptionTable().getAll()
                // Wait for a period of infrequent updates and return the latest update
                .debounce(SUBSCRIPTION_DEBOUNCE_INTERVAL, TimeUnit.MILLISECONDS)
                .share()            // Share allows multiple subscribers on the same observable
                .replay(1)          // Replay synchronizes subscribers to the last emitted result
                .autoConnect();
    }

    /**
     * Provides an observer to the latest update to the subscription table.
     * <p>
     * This observer may be subscribed multiple times, where each subscriber obtains
     * the latest synchronized changes available, effectively share the same data
     * across all subscribers.
     * <p>
     * This observer has a debounce cooldown, meaning if multiple updates are observed
     * in the cooldown interval, only the latest changes are emitted to the subscribers.
     * This reduces the amount of observations caused by frequent updates to the database.
     */
    @android.support.annotation.NonNull
    public Flowable<List<SubscriptionEntity>> getSubscription() {
        return subscription;
    }

    public Maybe<ChannelInfo> getChannelInfo(final SubscriptionEntity subscriptionEntity) {
        if (DEBUG) Log.d(TAG, "getChannelInfo() called with: subscriptionEntity = [" + subscriptionEntity + "]");

        return Maybe.fromSingle(ExtractorHelper
                .getChannelInfo(subscriptionEntity.getServiceId(), subscriptionEntity.getUrl(), false))
                .subscribeOn(subscriptionScheduler);
    }

    /**
     * Returns the database access interface for subscription table.
     */
    public SubscriptionDAO subscriptionTable() {
        return db.subscriptionDAO();
    }

    public Completable updateChannelInfo(final ChannelInfo info) {
        final Function<List<SubscriptionEntity>, CompletableSource> update = new Function<List<SubscriptionEntity>, CompletableSource>() {
            @Override
            public CompletableSource apply(@NonNull List<SubscriptionEntity> subscriptionEntities) throws Exception {
                if (DEBUG) Log.d(TAG, "updateChannelInfo() called with: subscriptionEntities = [" + subscriptionEntities + "]");
                if (subscriptionEntities.size() == 1) {
                    SubscriptionEntity subscription = subscriptionEntities.get(0);

                    // Subscriber count changes very often, making this check almost unnecessary.
                    // Consider removing it later.
                    if (!isSubscriptionUpToDate(info, subscription)) {
                        subscription.setData(info.name, info.avatar_url, info.description, info.subscriber_count);

                        return update(subscription);
                    }
                }

                return Completable.complete();
            }
        };

        return subscriptionTable().getSubscription(info.service_id, info.url)
                .firstOrError()
                .flatMapCompletable(update);
    }

    private Completable update(final SubscriptionEntity updatedSubscription) {
        return Completable.fromRunnable(new Runnable() {
            @Override
            public void run() {
                subscriptionTable().update(updatedSubscription);
            }
        });
    }

    private boolean isSubscriptionUpToDate(final ChannelInfo info, final SubscriptionEntity entity) {
        return info.url.equals(entity.getUrl()) &&
                info.service_id == entity.getServiceId() &&
                info.name.equals(entity.getName()) &&
                info.avatar_url.equals(entity.getAvatarUrl()) &&
                info.description.equals(entity.getDescription()) &&
                info.subscriber_count == entity.getSubscriberCount();
    }
}