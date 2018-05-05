package com.simplecity.amp_library.playback.salazar.exo;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.simplecity.amp_library.playback.salazar.utils.LogHelper;

/**
 * Created by fxsalazar
 * 01/02/2018.
 */

public class AudioFocusManager {
    private static final String TAG = LogHelper.makeLogTag(AudioFocusManager.class);
    private final AudioManager audioManager;
    private PlaybackCallback playbackCallback;
    private AudioFocusRequest audioFocusRequest;
    private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        LogHelper.d(TAG, "onAudioFocusChange. focusChange=", focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                playbackCallback.continuePlaybackOrUnDuckVolume();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest.willPauseWhenDucked()) {
                    playbackCallback.pausePlayback();
                } else {
                    playbackCallback.duckVolume();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                playbackCallback.pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                playbackCallback.pauseAndStopDelayed();
                break;
        }
    };

    public AudioFocusManager(@NonNull AudioManager audioManager, @NonNull PlaybackCallback playbackCallback) {
        this.audioManager = audioManager;
        this.playbackCallback = playbackCallback;
    }

    public int getAudioFocus() {
        return requestAudioFocus(audioManager);
    }

    public int giveUpAudioFocus() {
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

    public interface PlaybackCallback {
        void continuePlaybackOrUnDuckVolume();

        void duckVolume();

        void pausePlayback();

        void pauseAndStopDelayed();
    }
}
