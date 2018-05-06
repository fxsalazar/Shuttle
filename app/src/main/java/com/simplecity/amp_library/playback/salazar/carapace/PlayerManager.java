package com.simplecity.amp_library.playback.salazar.carapace;

import android.content.Context;
import android.media.AudioManager;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.simplecity.amp_library.playback.salazar.utils.LogHelper;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

/**
 * Created by fxsalazar
 * 04/02/2018.
 */
public class PlayerManager {
    private static final String TAG = LogHelper.makeLogTag(PlayerManager.class);
    private final MediaSessionCompat mediaSession;
    private CarapaceExoPlayer player;

    private final AudioFocusManager.PlaybackCallback audioFocusManagerPlaybackCallback = new AudioFocusManager.PlaybackCallback() {
        @Override
        public void continuePlaybackOrUnDuckVolume() {
            MediaControllerCompat mediaSessionController = mediaSession.getController();
            if (mediaSessionController.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                player.unDuckVolume();
            } else {
                mediaSessionController.getTransportControls().play();
            }
        }

        @Override
        public void duckVolume() {
            player.duckVolume();
        }

        @Override
        public void pausePlayback() {
            mediaSession.getController().getTransportControls().pause();
        }

        @Override
        public void pauseAndStopDelayed() {
            mediaSession.getController().getTransportControls().pause();
            // TODO: 04/02/2018 implement the delayed stoppage
//            delayedStopHandler.sendEmptyMessageDelayed(0, STOP_DELAY);
        }
    };

    public PlayerManager(MediaBrowserServiceCompat service, MediaSessionCompat mediaSession) {
        this.mediaSession = mediaSession;
        MediaNotificationManager mediaNotificationManager;
        try {
            mediaNotificationManager = new MediaNotificationManager(service);
        } catch (RemoteException e) {
            throw new IllegalStateException("Could not create a MediaNotificationManager", e);
        }
        player = CarapacePlayer.createInstance(ExoPlayerFactory.newSimpleInstance(service, new DefaultTrackSelector()));
        MediaSessionConnector mediaSessionConnector = new MediaSessionConnector(
                mediaSession,
                new DefaultAudioPlaybackController(
                        service,
                        mediaSession,
                        mediaNotificationManager,
                        new AudioFocusManager(getAudioManager(service), audioFocusManagerPlaybackCallback),
                        new DontBeNoisyBroadcastReceiver(mediaSession)));
        mediaSessionConnector.setPlayer(player, new DefaultAudioPlaybackPreparerController());
        mediaSessionConnector.setQueueEditor(new DefaultQueueEditor(mediaSession));
        mediaSessionConnector.setQueueNavigator(new DefaultQueueNavigator(service, mediaSession));

        player.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                Log.e(TAG, "onPlayerStateChanged: " + playWhenReady + " " + playbackState);
                switch (playbackState) {
                    case Player.STATE_IDLE:
                        Log.e(TAG, "onPlayerStateChanged: STATE_IDLE");
                        break;
                    case Player.STATE_BUFFERING:
                        Log.e(TAG, "onPlayerStateChanged: STATE_BUFFERING");
                        break;
                    case Player.STATE_READY:
                        Log.e(TAG, "onPlayerStateChanged: STATE_READY");
                        break;
                    case Player.STATE_ENDED:
                        mediaSession.getController().getTransportControls().skipToNext();
                        break;
                }

            }
        });
    }

    private AudioManager getAudioManager(Context context){
        return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }
}
