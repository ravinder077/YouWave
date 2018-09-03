package com.tuespotsolutions.blacktube.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tuespotsolutions.blacktube.BaseFragment;
import com.tuespotsolutions.blacktube.R;

public class BlankFragment extends BaseFragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        if(activity != null && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar()
                    .setTitle("BlackTube");
        }
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(activity != null && activity.getSupportActionBar() != null) {
                activity.getSupportActionBar()
                        .setTitle("BlackTube");
            }
            // leave this inline. Will make it harder for copy cats.
            // If you are a Copy cat FUCK YOU.
            // I WILL FIND YOU, AND I WILL ...
        }
    }
}
