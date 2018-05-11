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

package com.simplecity.amp_library.playback.salazar;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import com.simplecity.amp_library.playback.salazar.carapace.PlayerManager;
import com.simplecity.amp_library.playback.salazar.utils.LogHelper;
import com.simplecity.amp_library.ui.activities.ShortcutTrampolineActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.simplecity.amp_library.playback.salazar.utils.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static com.simplecity.amp_library.playback.salazar.utils.MediaIDHelper.MEDIA_ID_ROOT;


public class MusicService extends MediaBrowserServiceCompat {

    private static final String TAG = MusicService.class.getSimpleName();
    // Extra on MediaSession that contains the Cast device name currently connected to
    public static final String EXTRA_CONNECTED_CAST = "com.shuttle.CAST_NAME";
    // The action of the incoming Intent indicating that it contains a command
    // to be executed (see {@link #onStartCommand})
    public static final String ACTION_CMD = "com.shuttle.ACTION_CMD";
    // The key in the extras of the incoming Intent indicating the command that
    // should be executed (see {@link #onStartCommand})
    public static final String CMD_NAME = "CMD_NAME";
    // A value of a CMD_NAME key in the extras of the incoming Intent that
    // indicates that the music player should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // A value of a CMD_NAME key that indicates that the music player should switch
    // to local player from cast player.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    private MediaSessionCompat mediaSession;
    private PackageValidator packageValidator;
    private PlayerManager playerManager;
    private final MusicService.DelayedStopHandler delayedStopHandler = new MusicService.DelayedStopHandler(this);

    @Override
    public void onCreate() {
        super.onCreate();
        packageValidator = new PackageValidator(this);

        // Start a new MediaSession
        mediaSession = new MediaSessionCompat(this, MusicService.class.getSimpleName());
        mediaSession.setQueue(new ArrayList<>());
        setSessionToken(mediaSession.getSessionToken());
        playerManager = new PlayerManager(this, mediaSession);

        Context context = getApplicationContext();
        Intent intent = new Intent(context, ShortcutTrampolineActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setSessionActivity(pi);
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent startIntent, int flags, int startId) {
        if (startIntent != null) {
            String action = startIntent.getAction();
            String command = startIntent.getStringExtra(CMD_NAME);
            if (ACTION_CMD.equals(action)) {
//                if (CMD_PAUSE.equals(command)) {
//
//                    mediaControllerCompat.handlePauseRequest();
//                } else
                if (CMD_STOP_CASTING.equals(command)) {
//                    CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(mediaSession, startIntent);
            }
        }
        return START_STICKY;
    }

    /*
     * Handle case when user swipes the app away from the recents apps list by
     * stopping the service (and any ongoing player).
     */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    /**
     * (non-Javadoc)
     *
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mediaSession.getController().getTransportControls().stop();
        delayedStopHandler.removeCallbacksAndMessages(null);
        mediaSession.release();
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid,
                                 Bundle rootHints) {
        LogHelper.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName,
                "; clientUid=" + clientUid + " ; rootHints=", rootHints);
        // To ensure you are not allowing any arbitrary app to browse your app's contents, you
        // need to check the origin:
        if (!packageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            // If the request comes from an untrusted package, return an empty browser root.
            // If you return null, then the media browser will not be able to connect and
            // no further calls will be made to other media browsing methods.
            Log.i(TAG, "OnGetRoot: Browsing NOT ALLOWED for unknown caller. "
                    + "Returning empty browser root so all apps can use MediaController."
                    + clientPackageName);
            return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_EMPTY_ROOT, null);
        }

        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadItem(String itemId, @NonNull Result<MediaItem> result) {
        MediaDescriptionCompat description = new MediaDescriptionCompat
                .Builder()
                .setMediaId(itemId)
                .setTitle(itemId)
                .build();
        result.sendResult(new MediaItem(description, MediaItem.FLAG_BROWSABLE));
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
//        LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId);
//        if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
//            result.sendResult(new ArrayList<>());
//        } else if (musicProvider.isInitialized()) {
//            // if music library is ready, return immediately
//            result.sendResult(musicProvider.getChildren(parentMediaId, getResources()));
//        } else {
//            // otherwise, only return results when the music library is retrieved
//            result.detach();
//            musicProvider.retrieveMediaAsync(success -> result.sendResult(musicProvider.getChildren(parentMediaId, getResources())));
//        }
    }

    /**
     * A simple handler that stops the service if player is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.mediaSession != null) {
                service.mediaSession.getController().getTransportControls().stop();
//                if (service.mediaControllerCompat.getPlayback().isPlaying()) {
//                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
//                    return;
//                }
//                Log.d(TAG, "Stopping service with delay handler.");
//                service.stopSelf();
            }
        }
    }
}
