package com.simplecity.amp_library.playback.salazar.exo;

import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.simplecity.amp_library.playback.salazar.utils.LogHelper;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public final class DefaultAudioPlaybackPreparerController implements MediaSessionConnector.PlaybackPreparer {
    private static final String TAG = LogHelper.makeLogTag(DefaultAudioPlaybackPreparerController.class);

    @Override
    public long getSupportedPrepareActions() {
        return ACTIONS;
    }

    @Override
    public void onPrepare() {

    }

    @Override
    public void onPrepareFromMediaId(String mediaId, Bundle extras) {

    }

    @Override
    public void onPrepareFromSearch(String query, Bundle extras) {

    }

    @Override
    public void onPrepareFromUri(Uri uri, Bundle extras) {
        Log.e(TAG, "onPrepareFromUri: " + uri);
    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }
}
