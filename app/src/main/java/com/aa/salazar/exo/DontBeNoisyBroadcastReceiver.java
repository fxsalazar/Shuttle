package com.aa.salazar.exo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.aa.salazar.utils.LogHelper;

/**
 * Created by fxsalazar
 * 03/02/2018.
 */

public class DontBeNoisyBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LogHelper.makeLogTag(DontBeNoisyBroadcastReceiver.class);
    private final IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private MediaControllerCompat mediaController;
    private boolean isRegistered = false;

    public DontBeNoisyBroadcastReceiver(MediaSessionCompat mediaSession) {
        this.mediaController = mediaSession.getController();
    }

    public void registerBroadcast(Context context) {
        context.registerReceiver(this, intentFilter);
        isRegistered = true;
    }

    public void unregisterBroadcast(Context context) {
        if (isRegistered) {
            context.unregisterReceiver(this);
            isRegistered = false;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            LogHelper.d(TAG, "Headphones disconnected.");
            if (mediaController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
//                            Intent i = new Intent(context, MusicService.class);
//                            i.setAction(MusicService.ACTION_CMD);
//                            i.putExtra(MusicService.CMD_NAME, MusicService.CMD_PAUSE);
//                            context.startService(i);
                mediaController.getTransportControls().pause();
            }
        }
    }
}

