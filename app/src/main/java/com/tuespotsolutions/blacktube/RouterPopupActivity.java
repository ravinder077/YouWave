package com.tuespotsolutions.blacktube;

import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import extractor.NewPipe;
import extractor.StreamingService;
import extractor.exceptions.ExtractionException;
import com.tuespotsolutions.blacktube.player.PopupVideoPlayer;
import com.tuespotsolutions.blacktube.util.Constants;
import com.tuespotsolutions.blacktube.util.PermissionHelper;

/**
 * Get the url from the intent and open a popup player
 */
public class RouterPopupActivity extends RouterActivity {

    @Override
    protected void handleUrl(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && !PermissionHelper.checkSystemAlertWindowPermission(this)) {
            Toast.makeText(this, R.string.msg_popup_permission, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        StreamingService service;
        try {
            service = NewPipe.getServiceByUrl(url);
        } catch (ExtractionException e) {
            Toast.makeText(this, R.string.url_not_supported_toast, Toast.LENGTH_LONG).show();
            return;
        }

        Intent callIntent = new Intent(this, PopupVideoPlayer.class);
        switch (service.getLinkTypeByUrl(url)) {
            case STREAM:
                break;
            default:
                Toast.makeText(this, R.string.url_not_supported_toast, Toast.LENGTH_LONG).show();
                return;
        }

        callIntent.putExtra(Constants.KEY_URL, url);
        callIntent.putExtra(Constants.KEY_SERVICE_ID, service.getServiceId());
        startService(callIntent);

        finish();
    }
}
