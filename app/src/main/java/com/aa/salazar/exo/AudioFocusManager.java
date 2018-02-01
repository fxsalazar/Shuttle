package com.aa.salazar.exo;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.aa.salazar.playback.Playback;
import com.aa.salazar.utils.LogHelper;

/**
 * Created by fxsalazar
 * 01/02/2018.
 */

public class AudioFocusManager {
    private static final String TAG = LogHelper.makeLogTag(AudioFocusManager.class);
    private final AudioManager audioManager;
    private Playback playback;
    private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        LogHelper.d(TAG, "onAudioFocusChange. focusChange=", focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
//                currentAudioFocusState = AUDIO_FOCUSED;
                if (playback.getState() == PlaybackState.STATE_PAUSED) {
                    playback.play();
                } else {
                    // TODO: 01/02/2018 unduck the volume
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Audio focus was lost, but it's possible to duck (i.e.: play quietly)
//                currentAudioFocusState = AUDIO_NO_FOCUS_CAN_DUCK;
                // TODO: 01/02/2018 duck volume
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost audio focus, but will gain it back (shortly), so note whether
                // playback should resume
//                currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
//                playOnFocusGain = exoPlayer != null && exoPlayer.getPlayWhenReady();
                playback.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost audio focus, probably "permanently"
//                currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
                playback.pause();
                break;
        }

//        if (exoPlayer != null) {
//            // Update the player state based on the change
//            configurePlayerState();
//        }
    };
    private AudioFocusRequest audioFocusRequest;

    public AudioFocusManager(@NonNull AudioManager audioManager, @NonNull Playback playback) {
        this.audioManager = audioManager;
        this.playback = playback;
    }

    public int getAudioFocus() {
        LogHelper.d(TAG, "tryToGetAudioFocus");
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
////            currentAudioFocusState = AUDIO_FOCUSED;
//        } else {
////            currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
//        }
        return requestAudioFocus(audioManager);
    }

    public int giveUpAudioFocus() {
        LogHelper.d(TAG, "giveUpAudioFocus");

//        if (audioManager.abandonAudioFocus(onAudioFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
////            currentAudioFocusState = AUDIO_NO_FOCUS_NO_DUCK;
//        }
        return abandonAudioFocus(audioManager);
    }

    private int abandonAudioFocus(AudioManager audioManager) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        } else {
            return audioManager.abandonAudioFocusRequest(audioFocusRequest);
        }
    }

    private int requestAudioFocus(AudioManager audioManager) {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return requestAudioFocusMinSdk(audioManager);
        } else {
            return requestAudioFocus26(audioManager);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private int requestAudioFocus26(AudioManager audioManager) {
        AudioAttributes playbackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();
        AudioFocusRequest.Builder audioFocusRequestBuilder = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(onAudioFocusChangeListener)
                .setAudioAttributes(playbackAttributes);
        audioFocusRequest = audioFocusRequestBuilder.build();
        return audioManager.requestAudioFocus(audioFocusRequest);

    }

    private int requestAudioFocusMinSdk(AudioManager audioManager) {
        return audioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }
}
