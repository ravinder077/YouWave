package com.tuespotsolutions.blacktube.playlist;

import extractor.InfoItem;
import extractor.playlist.PlaylistInfo;
import extractor.playlist.PlaylistInfoItem;
import com.tuespotsolutions.blacktube.util.ExtractorHelper;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class PlaylistPlayQueue extends AbstractInfoPlayQueue<PlaylistInfo, PlaylistInfoItem> {
    public PlaylistPlayQueue(final PlaylistInfoItem item) {
        super(item);
    }

    public PlaylistPlayQueue(final int serviceId,
                             final String url,
                             final String nextPageUrl,
                             final List<InfoItem> streams,
                             final int index) {
        super(serviceId, url, nextPageUrl, streams, index);
    }

    @Override
    protected String getTag() {
        return "PlaylistPlayQueue@" + Integer.toHexString(hashCode());
    }

    @Override
    public void fetch() {
        if (this.isInitial) {
            ExtractorHelper.getPlaylistInfo(this.serviceId, this.baseUrl, false)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getHeadListObserver());
        } else {
            ExtractorHelper.getMorePlaylistItems(this.serviceId, this.baseUrl, this.nextUrl)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getNextItemsObserver());
        }
    }
}
