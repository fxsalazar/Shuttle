package com.simplecity.amp_library.playback.salazar.carapace;

import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.simplecity.amp_library.playback.salazar.MusicService;
import com.simplecity.amp_library.playback.salazar.utils.LogHelper;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.BehaviorProcessor;

/**
 * Created by fxsalazar
 * 01/02/2018.
 */

public final class DefaultAudioPlaybackController extends DefaultPlaybackController {

    private static final String TAG = LogHelper.makeLogTag(DefaultPlaybackController.class);
    private final AudioFocusManager audioFocusManager;
    @NonNull
    private final DontBeNoisyBroadcastReceiver dontBeNoisyBroadcastReceiver;
    @NonNull
    private final MediaBrowserServiceCompat service;
    private final MediaSessionCompat mediaSession;
    private final MediaNotificationManager mediaNotificationManager;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final BehaviorProcessor<PlaybackStateCompat> playbackStateProcessor = BehaviorProcessor.create();
    private final MediaControllerCompat.Callback callback = new MediaControllerCompat.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.w(TAG, "onPlaybackStateChanged: " + PlaybackStateCompatExtension.getReadableState(state));
            playbackStateProcessor.onNext(state);
        }
    };

    public DefaultAudioPlaybackController(
            @NonNull MediaBrowserServiceCompat service,
            @NonNull MediaSessionCompat mediaSession,
            @NonNull MediaNotificationManager mediaNotificationManager,
            @NonNull AudioFocusManager audioFocusManager,
            @NonNull DontBeNoisyBroadcastReceiver dontBeNoisyBroadcastReceiver) {
        this.service = service;
        this.mediaSession = mediaSession;
        this.mediaNotificationManager = mediaNotificationManager;
        this.audioFocusManager = audioFocusManager;
        this.dontBeNoisyBroadcastReceiver = dontBeNoisyBroadcastReceiver;

    }

    @Override
    public long getSupportedPlaybackActions(Player player) {
        if (player == null) {
            return 0;
        }
        long actions = ACTIONS;
        if (player.isCurrentWindowSeekable()) {
            actions |= PlaybackStateCompat.ACTION_SEEK_TO;
        }
        if (fastForwardIncrementMs > 0) {
            actions |= PlaybackStateCompat.ACTION_FAST_FORWARD;
        }
        if (rewindIncrementMs > 0) {
            actions |= PlaybackStateCompat.ACTION_REWIND;
        }
        return actions;
    }

    @Override
    public void onPlay(Player player) {
        Log.e(TAG, "onPlay: ");
        int audioFocus = audioFocusManager.getAudioFocus();
        switch (audioFocus) {
            case AudioManager.AUDIOFOCUS_REQUEST_FAILED:
                // TODO: 01/02/2018 what to do here?
                LogHelper.e(TAG, "AUDIOFOCUS_REQUEST_FAILED");
                break;
            case AudioManager.AUDIOFOCUS_REQUEST_GRANTED:
                MediaControllerCompat controller = mediaSession.getController();
                if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                    controller.registerCallback(callback);
                    // Prepare the first Item on the queue if any
                    controller.getTransportControls().skipToNext();
                    compositeDisposable.add(playbackStateProcessor
                            .filter(playbackStateCompat -> playbackStateCompat.getState() == PlaybackStateCompat.STATE_PAUSED)
                            .firstElement()
                            .subscribe(playbackState -> {
                                play(player);
                                controller.unregisterCallback(callback);
                            }));
                } else {
                    play(player);
                }
                break;
            case AudioManager.AUDIOFOCUS_REQUEST_DELAYED:
                // TODO: 04/02/2018 Maybe show a message
                Log.w(TAG, "onPlay: AUDIOFOCUS_REQUEST_DELAYED");
                break;
        }
    }

    private void play(Player player) {
        // start service
        service.startService(new Intent(service.getApplicationContext(), MusicService.class));
        // set media session active
        this.mediaSession.setActive(true);
        super.onPlay(player);
        // register noisy
        dontBeNoisyBroadcastReceiver.registerBroadcast(service);
        // start notification foreground
        mediaNotificationManager.startNotification();
    }

    @Override
    public void onPause(Player player) {
        super.onPause(player);
        // unregister noisy
        dontBeNoisyBroadcastReceiver.unregisterBroadcast(service);
        // stop notification foreground
        service.stopForeground(false);
    }

    @Override
    public void onStop(Player player) {
        if (audioFocusManager.giveUpAudioFocus() == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            super.onStop(player);
            // stop service
            service.stopSelf();
            // set media session active = false
            mediaSession.setActive(false);
            // unregister noisy
            dontBeNoisyBroadcastReceiver.unregisterBroadcast(service);
            // stop notification foreground
            mediaNotificationManager.stopNotification();

            // local stuff
            mediaSession.getController().unregisterCallback(callback);
            compositeDisposable.clear();
        }
    }

}
