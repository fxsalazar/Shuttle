package com.aa.salazar.exo;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

import com.aa.salazar.utils.LogHelper;

/**
 * Created by fxsalazar
 * 01/02/2018.
 */

public class AudioFocusManager {
    private static final String TAG = LogHelper.makeLogTag(AudioFocusManager.class);
    private final AudioManager audioManager;
    private ListenerCallback listenerCallback;
    private AudioFocusRequest audioFocusRequest;
    private final AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = focusChange -> {
        LogHelper.d(TAG, "onAudioFocusChange. focusChange=", focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                listenerCallback.continuePlaybackOrUnDuckVolume();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && audioFocusRequest.willPauseWhenDucked()) {
                    listenerCallback.pausePlayback();
                } else {
                    listenerCallback.duckVolume();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                listenerCallback.pausePlayback();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                listenerCallback.pauseAndStopDelayed();
                break;
        }
    };

    public AudioFocusManager(@NonNull AudioManager audioManager, @NonNull ListenerCallback listenerCallback) {
        this.audioManager = audioManager;
        this.listenerCallback = listenerCallback;
    }

    public int getAudioFocus() {
        LogHelper.d(TAG, "tryToGetAudioFocus");
        return requestAudioFocus(audioManager);
    }

    public int giveUpAudioFocus() {
        LogHelper.d(TAG, "giveUpAudioFocus");
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

    public interface ListenerCallback {
        void continuePlaybackOrUnDuckVolume();

        void duckVolume();

        void pausePlayback();

        void pauseAndStopDelayed();
    }
}
