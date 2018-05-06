package com.simplecity.amp_library.playback.salazar

import android.net.Uri
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.simplecity.amp_library.model.Album
import com.simplecity.amp_library.model.AlbumArtist
import com.simplecity.amp_library.model.Genre
import com.simplecity.amp_library.model.Song
import com.simplecity.amp_library.playback.MediaManager
import com.simplecity.amp_library.playback.QueueManager
import com.simplecity.amp_library.playback.salazar.carapace.Playback
import io.reactivex.Single

/**
 * Created by fxsalazar
 * 05/05/2018.
 */
class ExoMediaManager: MediaManager {
    override lateinit var playbackManager: Playback

    override fun playAll(songsSingle: Single<MutableList<Song>>, onEmpty: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playAll(songs: MutableList<Song>, position: Int, canClearShuffle: Boolean, onEmpty: (String) -> Unit) {

        songs.first().apply {
            val desc = MediaDescriptionCompat.Builder()
                    .setMediaId(this.id.toString())
                    .setTitle(this.name)
                    .setSubtitle(this.albumArtistName)
                    .setMediaUri(Uri.parse(this.path))
                    .build()

            val item = MediaSessionCompat.QueueItem(desc,this.id)
            playbackManager.play(item)
        }

    }

    override fun shuffleAll(songsSingle: Single<List<Song>>, onEmpty: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun shuffleAll(songs: MutableList<Song>, onEmpty: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playFile(uri: Uri?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getFilePath(): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isPlaying(): Boolean {
        return false
    }

    override fun getShuffleMode(): Int {
        return QueueManager.ShuffleMode.OFF
    }

    override fun setShuffleMode(mode: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRepeatMode(): Int {
        return QueueManager.RepeatMode.OFF
    }

    override fun next() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun previous(allowTrackRestart: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playOrPause() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAudioSessionId(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAlbumArtist(): AlbumArtist? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAlbum(): Album? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getSong(): Song? {
        return null
    }

    override fun getGenre(): Single<Genre> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getPosition(): Long {
        return 0
    }

    override fun getDuration(): Long {
        return 0
    }

    override fun seekTo(position: Long) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun moveQueueItem(from: Int, to: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggleShuffleMode() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cycleRepeat() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addToQueue(songs: MutableList<Song>, onAdded: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playNext(songsSingle: Single<List<Song>>, onAdded: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun playNext(songs: MutableList<Song>, onAdded: (String) -> Unit) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setQueuePosition(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun clearQueue() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getQueue(): MutableList<Song> {
        return mutableListOf()
    }

    override fun getQueuePosition(): Int {
        return 0
    }

    override fun removeFromQueue(position: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeFromQueue(songs: MutableList<Song>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toggleFavorite() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun closeEqualizerSessions(internal: Boolean, audioSessionId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun openEqualizerSession(internal: Boolean, audioSessionId: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateEqualizer() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}