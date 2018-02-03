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

package com.aa.salazar;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.media.MediaRouter;
import android.util.Log;

import com.aa.salazar.exo.AudioFocusManager;
import com.aa.salazar.exo.DefaultAudioPlaybackController;
import com.aa.salazar.exo.MediaNotificationManager;
import com.aa.salazar.exo.MusicProvider;
import com.aa.salazar.playback.CastPlayback;
import com.aa.salazar.playback.LocalPlayback;
import com.aa.salazar.playback.Playback;
import com.aa.salazar.playback.PlaybackManager;
import com.aa.salazar.playback.QueueManager;
import com.aa.salazar.utils.LogHelper;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.simplecity.amp_library.R;
import com.simplecity.amp_library.ui.activities.ShortcutTrampolineActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.aa.salazar.utils.MediaIDHelper.MEDIA_ID_EMPTY_ROOT;
import static com.aa.salazar.utils.MediaIDHelper.MEDIA_ID_ROOT;

public class MusicService extends MediaBrowserServiceCompat implements
        PlaybackManager.PlaybackServiceCallback {

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
    // indicates that the music playback should be paused (see {@link #onStartCommand})
    public static final String CMD_PAUSE = "CMD_PAUSE";
    // A value of a CMD_NAME key that indicates that the music playback should switch
    // to local playback from cast playback.
    public static final String CMD_STOP_CASTING = "CMD_STOP_CASTING";
    // Delay stopSelf by using a handler.
    private static final int STOP_DELAY = 30000;

    private MusicProvider musicProvider;
    private PlaybackManager playbackManager;

    private MediaSessionCompat mediaSession;
    private MediaNotificationManager mediaNotificationManager;
    private Bundle sessionExtras;
    private final DelayedStopHandler delayedStopHandler = new DelayedStopHandler(this);
    private MediaRouter mediaRouter;
    private PackageValidator packageValidator;
    private SessionManager castSessionManager;
    private SessionManagerListener<CastSession> castSessionManagerListener;

    private boolean isConnectedToCar;
    @Nullable
    private BroadcastReceiver carConnectionReceiver;
    private final AudioFocusManager.ListenerCallback audioFocusManagerListenerCallback = new AudioFocusManager.ListenerCallback() {


        @Override
        public void continuePlaybackOrUnDuckVolume() {
            MediaControllerCompat mediaSessionController = mediaSession.getController();
            if (mediaSessionController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                playback.unDuckVolume();
            } else {
                mediaSessionController.getTransportControls().play();
            }
        }

        @Override
        public void duckVolume() {
            playback.duckVolume();
        }

        @Override
        public void pausePlayback() {
            mediaSession.getController().getTransportControls().pause();
        }

        @Override
        public void pauseAndStopDelayed() {
            mediaSession.getController().getTransportControls().pause();
            delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        }
    };
    private Playback playback;
    private MediaSessionConnector mediaSessionConnector;

    /*
     * (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        musicProvider = new MusicProvider();

        // To make the app more responsive, fetch and cache catalog information now.
        // This can help improve the response time in the method
        // {@link #onLoadChildren(String, Result<List<MediaItem>>) onLoadChildren()}.
        musicProvider.retrieveMediaAsync(null /* Callback */);

        packageValidator = new PackageValidator(this);

        QueueManager queueManager = new QueueManager(musicProvider, getResources(),
                new QueueManager.MetadataUpdateListener() {
                    @Override
                    public void onMetadataChanged(MediaMetadataCompat metadata) {
                        mediaSession.setMetadata(metadata);
                    }

                    @Override
                    public void onMetadataRetrieveError() {
                        playbackManager.updatePlaybackState(
                                getString(R.string.error_no_metadata));
                    }

                    @Override
                    public void onCurrentQueueIndexUpdated(int queueIndex) {
                        playbackManager.handlePlayRequest();
                    }

                    @Override
                    public void onQueueUpdated(String title,
                                               List<MediaSessionCompat.QueueItem> newQueue) {
                        mediaSession.setQueue(newQueue);
                        mediaSession.setQueueTitle(title);
                    }
                });
        try {
            mediaNotificationManager = new MediaNotificationManager(this);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
//        playback = new LocalPlayback(this, musicProvider);
//        playbackManager = new PlaybackManager(
//                this,
//                getResources(),
//                musicProvider,
//                queueManager,
//                playback);

        // Start a new MediaSession
        mediaSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mediaSession.getSessionToken());
//        mediaSession.setCallback(playbackManager.getMediaSessionCallback());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
                | MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS);


        // TODO: 29/01/2018 setub car and wear helpers
//        sessionExtras = new Bundle();
//        CarHelper.setSlotReservationFlags(sessionExtras, true, true, true);
//        WearHelper.setSlotReservationFlags(sessionExtras, true, true);
//        WearHelper.setUseBackgroundFromTheme(sessionExtras, true);
//        mediaSession.setExtras(sessionExtras);

        mediaSessionConnector = new MediaSessionConnector(
                mediaSession,
                new DefaultAudioPlaybackController(
                        this,
                        mediaSession,
                        mediaNotificationManager,
                        playback,
                        audioFocusManagerListenerCallback));
        SimpleExoPlayer player = ExoPlayerFactory.newSimpleInstance(this, new DefaultTrackSelector());
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(
                        this, Util.getUserAgent(this, "uamp"), null);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        // The MediaSource represents the media to be played.

        MediaSource mediaSource =
                new ExtractorMediaSource(
                        Uri.parse("/storage/emulated/0/Music/03Encore.mp3"), dataSourceFactory, extractorsFactory, null, null);
        player.prepare(mediaSource);

        mediaSessionConnector.setPlayer(
                player,
                new MediaSessionConnector.PlaybackPreparer() {
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
                    public void onCommand(String command, Bundle extras, ResultReceiver cb) {

                    }
                });
//        playbackManager.updatePlaybackState(null);

        Context context = getApplicationContext();
        Intent intent = new Intent(context, ShortcutTrampolineActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, 99 /*request code*/,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mediaSession.setSessionActivity(pi);

        // TODO: 29/01/2018 setup castSessionManager
//        int playServicesAvailable =
//                GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
//        if (!TvHelper.isTvUiMode(this) && playServicesAvailable == ConnectionResult.SUCCESS) {
//            castSessionManager = CastContext.getSharedInstance(this).getSessionManager();
//            castSessionManagerListener = new CastSessionManagerListener();
//            castSessionManager.addSessionManagerListener(castSessionManagerListener,
//                    CastSession.class);
//        }

        mediaRouter = MediaRouter.getInstance(getApplicationContext());

        registerCarConnectionReceiver();
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
                if (CMD_PAUSE.equals(command)) {
                    playbackManager.handlePauseRequest();
                } else if (CMD_STOP_CASTING.equals(command)) {
                    CastContext.getSharedInstance(this).getSessionManager().endCurrentSession(true);
                }
            } else {
                // Try to handle the intent as a media button event wrapped by MediaButtonReceiver
                MediaButtonReceiver.handleIntent(mediaSession, startIntent);
            }
        }
        // Reset the delay handler to enqueue a message to stop the service if
        // nothing is playing.
        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        return START_STICKY;
    }

    /*
     * Handle case when user swipes the app away from the recents apps list by
     * stopping the service (and any ongoing playback).
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
        unregisterCarConnectionReceiver();
        // Service is being killed, so make sure we release our resources
//        playbackManager.handleStopRequest(null);
//        mediaNotificationManager.stopNotification();
//        mediaSession.getController().getTransportControls().stop();

        if (castSessionManager != null) {
            castSessionManager.removeSessionManagerListener(castSessionManagerListener,
                    CastSession.class);
        }

        delayedStopHandler.removeCallbacksAndMessages(null);
//        mediaSession.release();
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

        // TODO: 29/01/2018  what to do extra with valid package for Auto and Wear
//        //noinspection StatementWithEmptyBody
//        if (CarHelper.isValidCarPackage(clientPackageName)) {
//            // Optional: if your app needs to adapt the music library to show a different subset
//            // when connected to the car, this is where you should handle it.
//            // If you want to adapt other runtime behaviors, like tweak ads or change some behavior
//            // that should be different on cars, you should instead use the boolean flag
//            // set by the BroadcastReceiver carConnectionReceiver (isConnectedToCar).
//        }
//        //noinspection StatementWithEmptyBody
//        if (WearHelper.isValidWearCompanionPackage(clientPackageName)) {
//            // Optional: if your app needs to adapt the music library for when browsing from a
//            // Wear device, you should return a different MEDIA ROOT here, and then,
//            // on onLoadChildren, handle it accordingly.
//        }

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
        LogHelper.d(TAG, "OnLoadChildren: parentMediaId=", parentMediaId);
        if (MEDIA_ID_EMPTY_ROOT.equals(parentMediaId)) {
            result.sendResult(new ArrayList<MediaItem>());
        } else if (musicProvider.isInitialized()) {
            // if music library is ready, return immediately
            result.sendResult(musicProvider.getChildren(parentMediaId, getResources()));
        } else {
            // otherwise, only return results when the music library is retrieved
            result.detach();
            musicProvider.retrieveMediaAsync(new MusicProvider.Callback() {
                @Override
                public void onMusicCatalogReady(boolean success) {
                    result.sendResult(musicProvider.getChildren(parentMediaId, getResources()));
                }
            });
        }
    }

    /**
     * Callback method called from PlaybackManager whenever the music is about to play.
     */
    @Override
    public void onPlaybackStart() {
        mediaSession.setActive(true);

        delayedStopHandler.removeCallbacksAndMessages(null);

        // The service needs to continue running even after the bound client (usually a
        // MediaController) disconnects, otherwise the music playback will stop.
        // Calling startService(Intent) will keep the service running until it is explicitly killed.
        startService(new Intent(getApplicationContext(), MusicService.class));
    }


    /**
     * Callback method called from PlaybackManager whenever the music stops playing.
     */
    @Override
    public void onPlaybackStop() {
        mediaSession.setActive(false);
        // Reset the delayed stop handler, so after STOP_DELAY it will be executed again,
        // potentially stopping the service.
        delayedStopHandler.removeCallbacksAndMessages(null);
        delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        stopForeground(true);
    }

    @Override
    public void onNotificationRequired() {
        mediaNotificationManager.startNotification();
    }

    @Override
    public void onPlaybackStateUpdated(PlaybackStateCompat newState) {
        mediaSession.setPlaybackState(newState);
    }

    private void registerCarConnectionReceiver() {
        // TODO: 29/01/2018 register CarConnectionReceiver
//        IntentFilter filter = new IntentFilter(CarHelper.ACTION_MEDIA_STATUS);
//        carConnectionReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                String connectionEvent = intent.getStringExtra(CarHelper.MEDIA_CONNECTION_STATUS);
//                isConnectedToCar = CarHelper.MEDIA_CONNECTED.equals(connectionEvent);
//                Log.i(TAG, "Connection event to Android Auto: ", connectionEvent,
//                        " isConnectedToCar=", isConnectedToCar);
//            }
//        };
//        registerReceiver(carConnectionReceiver, filter);
    }

    private void unregisterCarConnectionReceiver() {
        if (carConnectionReceiver != null) {
            unregisterReceiver(carConnectionReceiver);
        }
    }

    /**
     * A simple handler that stops the service if playback is not active (playing)
     */
    private static class DelayedStopHandler extends Handler {
        private final WeakReference<MusicService> mWeakReference;

        private DelayedStopHandler(MusicService service) {
            mWeakReference = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            MusicService service = mWeakReference.get();
            if (service != null && service.playbackManager.getPlayback() != null) {
                if (service.playbackManager.getPlayback().isPlaying()) {
                    Log.d(TAG, "Ignoring delayed stop since the media player is in use.");
                    return;
                }
                Log.d(TAG, "Stopping service with delay handler.");
                service.stopSelf();
            }
        }
    }

    /**
     * Session Manager Listener responsible for switching the Playback instances
     * depending on whether it is connected to a remote player.
     */
    private class CastSessionManagerListener implements SessionManagerListener<CastSession> {

        @Override
        public void onSessionEnded(CastSession castSession, int error) {
            Log.d(TAG, "onSessionEnded");
            sessionExtras.remove(EXTRA_CONNECTED_CAST);
            mediaSession.setExtras(sessionExtras);
            Playback playback = new LocalPlayback(MusicService.this, musicProvider);
            mediaRouter.setMediaSessionCompat(null);
            playbackManager.switchToPlayback(playback, false);
        }

        @Override
        public void onSessionResumed(CastSession session, boolean wasSuspended) {
        }

        @Override
        public void onSessionStarted(CastSession castSession, String sessionId) {
            // In case we are casting, send the device name as an extra on MediaSession metadata.
            sessionExtras.putString(EXTRA_CONNECTED_CAST,
                    castSession.getCastDevice().getFriendlyName());
            mediaSession.setExtras(sessionExtras);
            // Now we can switch to CastPlayback
            Playback playback = new CastPlayback(musicProvider, MusicService.this);
            mediaRouter.setMediaSessionCompat(mediaSession);
            playbackManager.switchToPlayback(playback, true);
        }

        @Override
        public void onSessionStarting(CastSession session) {
        }

        @Override
        public void onSessionStartFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionEnding(CastSession session) {
            // This is our final chance to update the underlying stream position
            // In onSessionEnded(), the underlying CastPlayback#mRemoteMediaClient
            // is disconnected and hence we update our local value of stream position
            // to the latest position.
            playbackManager.getPlayback().updateLastKnownStreamPosition();
        }

        @Override
        public void onSessionResuming(CastSession session, String sessionId) {
        }

        @Override
        public void onSessionResumeFailed(CastSession session, int error) {
        }

        @Override
        public void onSessionSuspended(CastSession session, int reason) {
        }
    }
}
