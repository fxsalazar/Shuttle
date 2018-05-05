package com.simplecity.amp_library.playback.salazar.exo;

import android.support.v4.media.session.PlaybackStateCompat;

/**
 * Created by fxsalazar
 * 04/02/2018.
 */

public class PlaybackStateCompatExtension {
    public static String getReadableState(PlaybackStateCompat playbackStateCompat) {
        int stateState = playbackStateCompat.getState();
        switch (stateState) {
            case PlaybackStateCompat.STATE_NONE:
                return "PlaybackState.STATE_NONE";
            case PlaybackStateCompat.STATE_PAUSED:
                return "PlaybackState.STATE_PAUSED";
            case PlaybackStateCompat.STATE_CONNECTING:
                return "PlaybackState.STATE_CONNECTING";
            case PlaybackStateCompat.STATE_STOPPED:
                return "PlaybackState.STATE_STOPPED";
            case PlaybackStateCompat.STATE_BUFFERING:
                return "PlaybackState.STATE_BUFFERING";
            case PlaybackStateCompat.STATE_PLAYING:
                return "PlaybackState.STATE_PLAYING";
            case PlaybackStateCompat.STATE_ERROR:
                return "PlaybackState.STATE_ERROR";
            default:
                return "PlaybackState." + stateState;
        }
    }
}
