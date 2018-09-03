package com.tuespotsolutions.blacktube.fragments.list.playlist;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuespotsolutions.blacktube.R;
import extractor.ListExtractor;
import extractor.NewPipe;
import extractor.exceptions.ExtractionException;
import extractor.playlist.PlaylistInfo;
import extractor.stream.StreamInfoItem;
import com.tuespotsolutions.blacktube.fragments.list.BaseListInfoFragment;
import com.tuespotsolutions.blacktube.info_list.InfoItemDialog;
import com.tuespotsolutions.blacktube.playlist.PlayQueue;
import com.tuespotsolutions.blacktube.playlist.PlaylistPlayQueue;
import com.tuespotsolutions.blacktube.playlist.SinglePlayQueue;
import com.tuespotsolutions.blacktube.report.UserAction;
import com.tuespotsolutions.blacktube.util.ExtractorHelper;
import com.tuespotsolutions.blacktube.util.NavigationHelper;
import com.tuespotsolutions.blacktube.util.PermissionHelper;

import io.reactivex.Single;

import static com.tuespotsolutions.blacktube.util.AnimationUtils.animateView;

public class PlaylistFragment extends BaseListInfoFragment<PlaylistInfo> {

    /*//////////////////////////////////////////////////////////////////////////
    // Views
    //////////////////////////////////////////////////////////////////////////*/

    private View headerRootLayout;
    private TextView headerTitleView;
    private View headerUploaderLayout;
    private TextView headerUploaderName;
    private ImageView headerUploaderAvatar;
    private TextView headerStreamCount;
    private View playlistCtrl;

    private View headerPlayAllButton;
    private View headerPopupButton;
    private View headerBackgroundButton;

    public static PlaylistFragment getInstance(int serviceId, String url, String name) {
        PlaylistFragment instance = new PlaylistFragment();
        instance.setInitialData(serviceId, url, name);
        return instance;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // LifeCycle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist, container, false);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Init
    //////////////////////////////////////////////////////////////////////////*/

    protected View getListHeader() {
        headerRootLayout = activity.getLayoutInflater().inflate(R.layout.playlist_header, itemsList, false);
        headerTitleView = headerRootLayout.findViewById(R.id.playlist_title_view);
        headerUploaderLayout = headerRootLayout.findViewById(R.id.uploader_layout);
        headerUploaderName = headerRootLayout.findViewById(R.id.uploader_name);
        headerUploaderAvatar = headerRootLayout.findViewById(R.id.uploader_avatar_view);
        headerStreamCount = headerRootLayout.findViewById(R.id.playlist_stream_count);
        playlistCtrl = headerRootLayout.findViewById(R.id.playlist_control);

        headerPlayAllButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_all_button);
        headerPopupButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_popup_button);
        headerBackgroundButton = headerRootLayout.findViewById(R.id.playlist_ctrl_play_bg_button);

        return headerRootLayout;
    }

    @Override
    protected void initViews(View rootView, Bundle savedInstanceState) {
        super.initViews(rootView, savedInstanceState);

        infoListAdapter.useMiniItemVariants(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (DEBUG) Log.d(TAG, "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_playlist, menu);
    }

    @Override
    protected void showStreamDialog(final StreamInfoItem item) {
        final Context context = getContext();
        if (context == null || context.getResources() == null || getActivity() == null) return;

        final String[] commands = new String[]{
                context.getResources().getString(R.string.enqueue_on_background),
                context.getResources().getString(R.string.enqueue_on_popup),
                context.getResources().getString(R.string.start_here_on_main),
                context.getResources().getString(R.string.start_here_on_background),
                context.getResources().getString(R.string.start_here_on_popup),
        };

        final DialogInterface.OnClickListener actions = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final int index = Math.max(infoListAdapter.getItemsList().indexOf(item), 0);
                switch (i) {
                    case 0:
                        NavigationHelper.enqueueOnBackgroundPlayer(context, new SinglePlayQueue(item));
                        break;
                    case 1:
                        NavigationHelper.enqueueOnPopupPlayer(context, new SinglePlayQueue(item));
                        break;
                    case 2:
                        NavigationHelper.playOnMainPlayer(context, getPlayQueue(index));
                        break;
                    case 3:
                        NavigationHelper.playOnBackgroundPlayer(context, getPlayQueue(index));
                        break;
                    case 4:
                        NavigationHelper.playOnPopupPlayer(context, getPlayQueue(index));
                        break;
                    default:
                        break;
                }
            }
        };

        new InfoItemDialog(getActivity(), item, commands, actions).show();
    }
    /*//////////////////////////////////////////////////////////////////////////
    // Load and handle
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected Single<ListExtractor.NextItemsResult> loadMoreItemsLogic() {
        return ExtractorHelper.getMorePlaylistItems(serviceId, url, currentNextItemsUrl);
    }

    @Override
    protected Single<PlaylistInfo> loadResult(boolean forceLoad) {
        return ExtractorHelper.getPlaylistInfo(serviceId, url, forceLoad);
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Contract
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void showLoading() {
        super.showLoading();
        animateView(headerRootLayout, false, 200);
        animateView(itemsList, false, 100);

        imageLoader.cancelDisplayTask(headerUploaderAvatar);
        animateView(headerUploaderLayout, false, 200);
    }

    @Override
    public void handleResult(@NonNull final PlaylistInfo result) {
        super.handleResult(result);

        animateView(headerRootLayout, true, 100);
        animateView(headerUploaderLayout, true, 300);
        headerUploaderLayout.setOnClickListener(null);
        if (!TextUtils.isEmpty(result.uploader_name)) {
            headerUploaderName.setText(result.uploader_name);
            if (!TextUtils.isEmpty(result.uploader_url)) {
                headerUploaderLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        NavigationHelper.openChannelFragment(getFragmentManager(), result.service_id, result.uploader_url, result.uploader_name);
                    }
                });
            }
        }

        playlistCtrl.setVisibility(View.VISIBLE);

        imageLoader.displayImage(result.uploader_avatar_url, headerUploaderAvatar, DISPLAY_AVATAR_OPTIONS);
        headerStreamCount.setText(getResources().getQuantityString(R.plurals.videos, (int) result.stream_count, (int) result.stream_count));

        if (!result.errors.isEmpty()) {
            showSnackBarError(result.errors, UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(result.service_id), result.url, 0);
        }

        headerPlayAllButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationHelper.playOnMainPlayer(activity, getPlayQueue());
            }
        });
        headerPopupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !PermissionHelper.checkSystemAlertWindowPermission(activity)) {
                    Toast toast = Toast.makeText(activity, R.string.msg_popup_permission, Toast.LENGTH_LONG);
                    TextView messageView = toast.getView().findViewById(android.R.id.message);
                    if (messageView != null) messageView.setGravity(Gravity.CENTER);
                    toast.show();
                    return;
                }
                NavigationHelper.playOnPopupPlayer(activity, getPlayQueue());
            }
        });
        headerBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavigationHelper.playOnBackgroundPlayer(activity, getPlayQueue());
            }
        });
    }

    private PlayQueue getPlayQueue() {
        return getPlayQueue(0);
    }

    private PlayQueue getPlayQueue(final int index) {
        return new PlaylistPlayQueue(
                currentInfo.service_id,
                currentInfo.url,
                currentInfo.next_streams_url,
                infoListAdapter.getItemsList(),
                index
        );
    }

    @Override
    public void handleNextItems(ListExtractor.NextItemsResult result) {
        super.handleNextItems(result);

        if (!result.errors.isEmpty()) {
            showSnackBarError(result.errors, UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId)
                    , "Get next page of: " + url, 0);
        }
    }

    /*//////////////////////////////////////////////////////////////////////////
    // OnError
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    protected boolean onError(Throwable exception) {
        if (super.onError(exception)) return true;

        int errorId = exception instanceof ExtractionException ? R.string.parsing_error : R.string.general_error;
        onUnrecoverableError(exception, UserAction.REQUESTED_PLAYLIST, NewPipe.getNameOfService(serviceId), url, errorId);
        return true;
    }

    /*//////////////////////////////////////////////////////////////////////////
    // Utils
    //////////////////////////////////////////////////////////////////////////*/

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        headerTitleView.setText(title);
    }
}
