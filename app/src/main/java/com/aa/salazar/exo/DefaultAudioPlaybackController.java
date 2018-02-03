package com.aa.salazar.exo;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaSessionCompat;

import com.aa.salazar.MusicService;
import com.aa.salazar.playback.Playback;
import com.aa.salazar.utils.LogHelper;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.DefaultPlaybackController;

/**
 * Created by fxsalazar
 * 01/02/2018.
 */

public final class DefaultAudioPlaybackController extends DefaultPlaybackController {

    private static final String TAG = LogHelper.makeLogTag(DefaultPlaybackController.class);
    private final AudioFocusManager audioFocusManager;
    @NonNull
    private final MediaBrowserServiceCompat service;
    private final MediaSessionCompat mediaSession;
    private final MediaNotificationManager mediaNotificationManager;
    private final AudioManager audioManager;
    private Playback playback;

    public DefaultAudioPlaybackController(
            @NonNull MediaBrowserServiceCompat service,
            @NonNull MediaSessionCompat mediaSession,
            @NonNull MediaNotificationManager mediaNotificationManager,
            @NonNull Playback playback,
            @NonNull AudioFocusManager.ListenerCallback audioFocusManagerListenerCallback) {
        this.service = service;
        this.mediaSession = mediaSession;
        this.mediaNotificationManager = mediaNotificationManager;
        audioManager = (AudioManager) service.getSystemService(Context.AUDIO_SERVICE);
        this.playback = playback;
        audioFocusManager = new AudioFocusManager(audioManager, audioFocusManagerListenerCallback);
    }

    @Override
    public void onPlay(Player player) {
        int audioFocus = audioFocusManager.getAudioFocus();
        if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            // TODO: 01/02/2018 what to do here?
            LogHelper.e(TAG, "Request not granted");
        } else {
            // Prepare for AUDIOFOCUS_REQUEST_DELAYED
            prepareForPlayback();
            if (audioFocus == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                super.onPlay(player);
            }
        }
    }

    private void prepareForPlayback() {
        // start service
        service.startService(new Intent(service.getApplicationContext(), MusicService.class));
        // set media session active
        this.mediaSession.setActive(true);
        // register noisy
        // start notification foreground
//        mediaNotificationManager.startNotification();
    }

    @Override
    public void onPause(Player player) {
        super.onPause(player);
        // unregister noisy
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
            // stop notification foreground
            mediaNotificationManager.stopNotification();
        }

    }

}
