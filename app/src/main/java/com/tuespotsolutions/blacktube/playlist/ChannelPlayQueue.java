package com.tuespotsolutions.blacktube.playlist;

import extractor.InfoItem;
import extractor.channel.ChannelInfo;
import extractor.channel.ChannelInfoItem;
import com.tuespotsolutions.blacktube.util.ExtractorHelper;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public final class ChannelPlayQueue extends AbstractInfoPlayQueue<ChannelInfo, ChannelInfoItem> {
    public ChannelPlayQueue(final ChannelInfoItem item) {
        super(item);
    }

    public ChannelPlayQueue(final int serviceId,
                            final String url,
                            final String nextPageUrl,
                            final List<InfoItem> streams,
                            final int index) {
        super(serviceId, url, nextPageUrl, streams, index);
    }

    @Override
    protected String getTag() {
        return "ChannelPlayQueue@" + Integer.toHexString(hashCode());
    }

    @Override
    public void fetch() {
        if (this.isInitial) {
            ExtractorHelper.getChannelInfo(this.serviceId, this.baseUrl, false)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getHeadListObserver());
        } else {
            ExtractorHelper.getMoreChannelItems(this.serviceId, this.baseUrl, this.nextUrl)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(getNextItemsObserver());
        }
    }
}
