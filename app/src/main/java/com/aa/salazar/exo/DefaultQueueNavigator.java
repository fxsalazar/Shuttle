package com.aa.salazar.exo;

import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;

import com.aa.salazar.utils.LogHelper;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import io.reactivex.functions.Consumer;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public class DefaultQueueNavigator implements MediaSessionConnector.QueueNavigator {

    private static final String TAG = LogHelper.makeLogTag(DefaultQueueNavigator.class);
    private final MediaSessionCompat mediaSession;
    private final ExtractorMediaSource.Factory factory;
    private final AtomicLong activeItem = new AtomicLong();
    private final AtomicInteger activePosition = new AtomicInteger(-1);

    public DefaultQueueNavigator(Context context, MediaSessionCompat mediaSession) {
        this.mediaSession = mediaSession;
        this.factory = new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(context, Util.getUserAgent(context, "Shuttle")));
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
        Log.e(TAG, "onCurrentWindowIndexChanged: ");
    }

    @Override
    public long getActiveQueueItemId(@Nullable Player player) {
        return activeItem.get();
    }

    @Override
    public void onSkipToPrevious(Player player) {
        Log.e(TAG, "onSkipToPrevious: ");
        getMediaSessionQueue(queueItems -> {
            if (player.getCurrentPosition() > 5000 || activePosition.get() <= 0) {
                player.seekToDefaultPosition();
            } else {
                playQueueItem(player, queueItems.get(activePosition.decrementAndGet()));
            }
        });
    }

    @Override
    public void onSkipToQueueItem(Player player, long id) {
        getMediaSessionQueue(queueItems -> {
            for (MediaSessionCompat.QueueItem queueItem : queueItems) {
                if (Long.parseLong(queueItem.getDescription().getMediaId()) == id) {
                    playQueueItem(player, queueItem);
                    activePosition.set(queueItems.indexOf(queueItem));
                    return;
                }
            }
            Log.e(TAG, "onSkipToQueueItem: Item not found; Check QueueItem's Ids");
        });
    }

    @Override
    public void onSkipToNext(Player player) {
        Log.e(TAG, "onSkipToNext: ");
        getMediaSessionQueue(queueItems -> playQueueItem(player, queueItems.get(activePosition.incrementAndGet())));
    }

    private void playQueueItem(Player player, MediaSessionCompat.QueueItem queueItem) {
        activeItem.set(Long.parseLong(queueItem.getDescription().getMediaId()));
        ((ExoPlayer) player).prepare(factory.createMediaSource(queueItem.getDescription().getMediaUri()));
    }

    private void getMediaSessionQueue(@NonNull Consumer<List<MediaSessionCompat.QueueItem>> doWith) {
        MediaControllerCompat controller = mediaSession.getController();
        List<MediaSessionCompat.QueueItem> queue = controller.getQueue();
        if (queue != null && !queue.isEmpty()) {
            try {
                doWith.accept(queue);
            } catch (Exception e) {
                Log.e(TAG, "getMediaSessionQueue: ", e);
            }
        } else {
            Log.e(TAG, "getMediaSessionQueue: Queue is null/empty");
        }
    }
}
