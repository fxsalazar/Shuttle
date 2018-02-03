package com.aa.salazar.exo;

import com.google.android.exoplayer2.ExoPlayer;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public interface ShuttleExoPlayer extends ExoPlayer {
    void unDuckVolume();

    void duckVolume();
}
