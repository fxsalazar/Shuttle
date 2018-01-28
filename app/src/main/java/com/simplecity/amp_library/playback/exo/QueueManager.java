/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.simplecity.amp_library.playback.exo;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import java.util.List;

/**
 * Simple data provider for queues. Keeps track of a current queue and a current index in the
 * queue. Also provides methods to set the current queue based on common queries, relying on a
 * given MusicProvider to provide the actual media metadata.
 */
public class QueueManager {


    public QueueManager(MusicProvider mMusicProvider, Resources resources, MetadataUpdateListener metadataUpdateListener) {

    }

    public MediaSessionCompat.QueueItem getCurrentMusic() {
        return null;
    }

    public boolean skipQueuePosition(int i) {
        return false;
    }

    public void updateMetadata() {

    }

    public void setQueueFromMusic(String mediaId) {

    }

    public void setRandomQueue() {

    }

    public void setCurrentQueueItem(long queueId) {

    }

    public boolean setQueueFromSearch(String query, Bundle extras) {
        return false;
    }

    public interface MetadataUpdateListener {
        void onMetadataChanged(MediaMetadataCompat metadata);

        void onMetadataRetrieveError();

        void onCurrentQueueIndexUpdated(int queueIndex);

        void onQueueUpdated(String title, List<MediaSessionCompat.QueueItem> newQueue);
    }
}
