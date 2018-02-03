package com.aa.salazar.exo;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.aa.salazar.utils.LogHelper;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public final class DefaultQueueEditor implements MediaSessionConnector.QueueEditor {
    private static final String TAG = LogHelper.makeLogTag(DefaultQueueEditor.class);
    private MediaSessionCompat mediaSession;
    private final DataSource.Factory dataSourceFactory;

    public DefaultQueueEditor(MediaSessionCompat mediaSession, Context context) {
        this.mediaSession = mediaSession;
        this.dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Shuttle"));
    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }

    @Override
    public long getSupportedQueueEditorActions(@Nullable Player player) {
        return ACTIONS;
    }

    @Override
    public void onAddQueueItem(Player player, MediaDescriptionCompat description) {
        List<MediaSessionCompat.QueueItem> queue = mediaSession.getController().getQueue();
        MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, description.describeContents());
        queue.add(queueItem);
        mediaSession.setQueue(queue);
//        ((ExoPlayer) player).prepare(new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(description.getMediaUri()));
    }

    @Override
    public void onAddQueueItem(Player player, MediaDescriptionCompat description, int index) {
        Log.e(TAG, "onAddQueueItem: ");
    }

    @Override
    public void onRemoveQueueItem(Player player, MediaDescriptionCompat description) {

    }

    @Override
    public void onRemoveQueueItemAt(Player player, int index) {

    }

    @Override
    public void onSetRating(Player player, RatingCompat rating) {

    }
}
