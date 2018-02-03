package com.aa.salazar.exo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.aa.salazar.utils.LogHelper;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public class DefaultQueueNavigator implements MediaSessionConnector.QueueNavigator {

    private static final String TAG = LogHelper.makeLogTag(DefaultQueueNavigator.class);
    private final MediaSessionCompat mediaSession;
    private final DataSource.Factory dataSourceFactory;
    private ExtractorMediaSource.Factory factory;

    public DefaultQueueNavigator(Context context, MediaSessionCompat mediaSession) {
        this.mediaSession = mediaSession;
        this.dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Shuttle"));
        this.factory = new ExtractorMediaSource.Factory(dataSourceFactory);
    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }

    @Override
    public long getSupportedQueueNavigatorActions(@Nullable Player player) {
        return ACTIONS;
    }

    @Override
    public void onTimelineChanged(Player player) {
        Log.e(TAG, "onTimelineChanged: ");
    }

    @Override
    public void onCurrentWindowIndexChanged(Player player) {

    }

    @Override
    public long getActiveQueueItemId(@Nullable Player player) {
        return 0;
    }

    @Override
    public void onSkipToPrevious(Player player) {

    }

    @Override
    public void onSkipToQueueItem(Player player, long id) {

    }

    @Override
    public void onSkipToNext(Player player) {
        Log.e(TAG, "onSkipToNext: ");
        MediaControllerCompat controller = mediaSession.getController();
        Uri path = controller.getQueue().get(1).getDescription().getMediaUri();
//        Uri parse = Uri.parse("/storage/emulated/0/Music/04 Show Me What You Got.m4a");

        // The MediaSource represents the media to be played.
        ((ExoPlayer) player).prepare(factory.createMediaSource(path));
    }
}
