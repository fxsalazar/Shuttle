package com.simplecity.amp_library.playback.salazar.exo;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.simplecity.amp_library.playback.salazar.utils.LogHelper;
import io.reactivex.functions.Consumer;

import java.util.List;
import java.util.ListIterator;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public final class DefaultQueueEditor implements MediaSessionConnector.QueueEditor {
    private static final String TAG = LogHelper.makeLogTag(DefaultQueueEditor.class);
    private static final int TAIL_OF_THE_QUEUE = -1000;
    private MediaSessionCompat mediaSession;

    public DefaultQueueEditor(MediaSessionCompat mediaSession) {
        this.mediaSession = mediaSession;
    }

    @Override
    public String[] getCommands() {
        return new String[0];
    }

    @Override
    public void onCommand(Player player, String command, Bundle extras, ResultReceiver cb) {

    }

    @Override
    public void onAddQueueItem(Player player, MediaDescriptionCompat description) {
        onAddQueueItem(player, description, TAIL_OF_THE_QUEUE);
    }

    @Override
    public void onAddQueueItem(Player player, MediaDescriptionCompat description, int index) {
        editQueue(queueItems -> {
            MediaSessionCompat.QueueItem queueItem = new MediaSessionCompat.QueueItem(description, Long.parseLong(description.getMediaId()));
            if (index == TAIL_OF_THE_QUEUE) {
                queueItems.add(queueItem);
            } else {
                queueItems.add(index, queueItem);
            }
        });
    }

    @Override
    public void onRemoveQueueItem(Player player, MediaDescriptionCompat description) {
        editQueue(queueItems -> {
            ListIterator<MediaSessionCompat.QueueItem> queueItemListIterator = queueItems.listIterator();
            MediaSessionCompat.QueueItem queueItem;
            while (queueItemListIterator.hasNext()) {
                queueItem = queueItemListIterator.next();
                if (queueItem.getDescription().getMediaId().equals(description.getMediaId())) {
                    queueItemListIterator.remove();
                    return;
                }
            }
        });
    }

    @Override
    public void onRemoveQueueItemAt(Player player, int index) {
        editQueue(queueItems -> queueItems.remove(index));
    }

    private void editQueue(Consumer<List<MediaSessionCompat.QueueItem>> doWith) {
        List<MediaSessionCompat.QueueItem> queue = mediaSession.getController().getQueue();
        try {
            doWith.accept(queue);
            mediaSession.setQueue(queue);
        } catch (Exception e) {
            Log.e(TAG, "editQueue: ", e);
        }
    }
}
