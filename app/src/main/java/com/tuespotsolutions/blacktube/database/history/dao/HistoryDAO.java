package com.tuespotsolutions.blacktube.database.history.dao;

import com.tuespotsolutions.blacktube.database.BasicDAO;

public interface HistoryDAO<T> extends BasicDAO<T> {
    T getLatestEntry();
}
