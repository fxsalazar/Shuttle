package com.aa.salazar.exo;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.aa.salazar.MusicService;
import com.aa.salazar.utils.LogHelper;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

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
    private final AudioManager audioManager;
    private final BehaviorSubject<Integer> playbackStateChangedBehaviorSubject = BehaviorSubject.create();

    public DefaultAudioPlaybackController(
            @NonNull MediaBrowserServiceCompat service,
            @NonNull MediaSessionCompat mediaSession,
            @NonNull MediaNotificationManager mediaNotificationManager,
            @NonNull AudioFocusManager.ListenerCallback audioFocusManagerListenerCallback,
            @NonNull DontBeNoisyBroadcastReceiver dontBeNoisyBroadcastReceiver) {
        this.service = service;
        this.mediaSession = mediaSession;
        this.mediaNotificationManager = mediaNotificationManager;
        audioManager = (AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        audioFocusManager = new AudioFocusManager(audioManager, audioFocusManagerListenerCallback);
        this.dontBeNoisyBroadcastReceiver = dontBeNoisyBroadcastReceiver;

        Observable<MediaDescriptionCompat> xx = Observable.create(emitter -> {
            mediaSession.getController().registerCallback(new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    emitter.onNext(metadata.getDescription());
                }
            });
        });

        mediaSession.getController().registerCallback(new MediaControllerCompat.Callback() {
            @Override
            public void onPlaybackStateChanged(PlaybackStateCompat state) {
                int stateState = state.getState();
                switch (stateState) {
                    case PlaybackStateCompat.STATE_NONE:
                        Log.e(TAG, "======> onPlaybackStateChanged: NONE");
                        break;
                    case PlaybackStateCompat.STATE_PAUSED:
                        Log.e(TAG, "======> onPlaybackStateChanged: STATE_PAUSED");
                        break;
                    case PlaybackStateCompat.STATE_CONNECTING:
                        Log.e(TAG, "======> onPlaybackStateChanged: STATE_CONNECTING");
                        break;
                    case PlaybackStateCompat.STATE_STOPPED:
                        Log.e(TAG, "======> onPlaybackStateChanged: STATE_STOPPED");
                        break;
                    case PlaybackStateCompat.STATE_BUFFERING:
                        Log.e(TAG, "======> onPlaybackStateChanged: STATE_BUFFERING");
                        break;
                    case PlaybackStateCompat.STATE_PLAYING:
                        Log.e(TAG, "======> onPlaybackStateChanged: STATE_PLAYING");
                        break;
                    default:
                        Log.e(TAG, "======> onPlaybackStateChanged: " + stateState);

                }
                playbackStateChangedBehaviorSubject.onNext(stateState);

            }
        });
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
        if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            // TODO: 01/02/2018 what to do here?
            LogHelper.e(TAG, "Request not granted");
        } else if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            MediaControllerCompat controller = mediaSession.getController();
            if (controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_NONE) {
                // Prepare the first Item on the queue if any
                controller.getTransportControls().skipToNext();

                playbackStateChangedBehaviorSubject
                        .filter(integer -> integer == PlaybackStateCompat.STATE_PAUSED)
                        .firstElement()
                        .subscribe(playbackState -> play(player));
            } else {
                play(player);
            }
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
        }

    }

}
