package com.simplecity.amp_library.playback.salazar

import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import com.simplecity.amp_library.playback.salazar.carapace.Playback

/**
 * Created by fxsalazar
 * 05/05/2018.
 */
class ExoMediaPlayback(private val mediaControllerCompat: MediaControllerCompat?): Playback {
    override fun start() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun stop(notifyListeners: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setState(state: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getState(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isConnected(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isPlaying(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCurrentStreamPosition(): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateLastKnownStreamPosition() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun play(item: MediaSessionCompat.QueueItem?) {
        mediaControllerCompat?.addQueueItem(item?.description)
        mediaControllerCompat?.transportControls?.play()
    }

    override fun play() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun seekTo(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setCurrentMediaId(mediaId: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getCurrentMediaId(): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setVolume(volume: Float) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun unDuckVolume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun duckVolume() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun pauseAndDelayedStop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setCallback(callback: Playback.Callback?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}