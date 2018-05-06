package com.simplecity.amp_library.playback.salazar.carapace;

import com.google.android.exoplayer2.ExoPlayer;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public interface CarapaceExoPlayer extends ExoPlayer {
    void unDuckVolume();

    void duckVolume();
}
