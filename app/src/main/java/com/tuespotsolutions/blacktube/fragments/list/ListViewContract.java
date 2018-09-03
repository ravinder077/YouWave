package com.tuespotsolutions.blacktube.fragments.list;

import com.tuespotsolutions.blacktube.fragments.ViewContract;

public interface ListViewContract<I, N> extends ViewContract<I> {
    void showListFooter(boolean show);

    void handleNextItems(N result);
}
