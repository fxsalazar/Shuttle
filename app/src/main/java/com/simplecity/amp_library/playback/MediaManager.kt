package com.simplecity.amp_library.playback

import android.net.Uri
import android.support.v4.media.session.MediaControllerCompat
import com.simplecity.amp_library.model.Album
import com.simplecity.amp_library.model.AlbumArtist
import com.simplecity.amp_library.model.Genre
import com.simplecity.amp_library.model.Song
import io.reactivex.Single

interface MediaManager {

    companion object Defs {
        const val ADD_TO_PLAYLIST = 0
        const val PLAYLIST_SELECTED = 1
        const val NEW_PLAYLIST = 2
    }

    var mediaControllerCompat: MediaControllerCompat

    fun registerCallback(callback: MediaControllerCompat.Callback)
    fun unregisterCallback(callback: MediaControllerCompat.Callback)

    /**
     * Sends a list of songs to the MusicService for playback
     */
    fun playAll(songsSingle: Single<MutableList<Song>>, onEmpty: (String) -> Unit)

    /**
     * Sends a list of songs to the MusicService for playback
     */
    fun playAll(songs: MutableList<Song>, position: Int, canClearShuffle: Boolean, onEmpty: (String) -> Unit)

    /**
     * Shuffles all songs in the given song list
     */
    fun shuffleAll(songsSingle: Single<List<Song>>, onEmpty: (String) -> Unit)

    /**
     * Shuffles all songs in the given list
     */
    fun shuffleAll(songs: MutableList<Song>, onEmpty: (String) -> Unit)

    /**
     * @param uri The source of the file
     */
    fun playFile(uri: Uri?)

    /**
     * @return [String] The path to the currently playing file
     */
    fun getFilePath(): String?

    /**
     * @return True if we're playing music, false otherwise.
     */
    fun isPlaying(): Boolean

    /**
     * @return The current shuffle mode
     */
    @QueueManager.ShuffleMode
    fun getShuffleMode(): Int

    /**
     * Sets the shuffle mode
     */
    fun setShuffleMode(mode: Int)

    /**
     * @return The current repeat mode
     */
    @QueueManager.RepeatMode
    fun getRepeatMode(): Int

    /**
     * Changes to the next track
     */
    fun next()

    /**
     * Changes to the previous track
     *
     * @param allowTrackRestart if true, the track will restart if the track position is > 2 seconds
     */
    fun previous(allowTrackRestart: Boolean)

    /**
     * Plays or pauses the music depending on the current state.
     */
    fun playOrPause()

    fun getAudioSessionId(): Int

    /**
     * Note: This does not return a fully populated album artist.
     *
     * @return a partial [AlbumArtist] containing a partial [Album]
     * which contains the current song.
     */
    fun getAlbumArtist(): AlbumArtist?

    /**
     * Note: This does not return a fully populated album.
     *
     * @return a partial [Album] containing this song.
     */
    fun getAlbum(): Album?

    fun getSong(): Song?

    fun getGenre(): Single<Genre>

    /**
     * Method getPosition.
     *
     * @return [long]
     */
    fun getPosition(): Long

    /**
     * Method duration.
     *
     * @return [long]
     */
    fun getDuration(): Long

    /**
     * Method seekTo.
     *
     * @param position the [long] position to seek to
     */
    fun seekTo(position: Long)

    fun moveQueueItem(from: Int, to: Int)

    fun toggleShuffleMode()

    fun cycleRepeat()

    fun addToQueue(songs: MutableList<Song>, onAdded: (String) -> Unit)

    fun playNext(songsSingle: Single<List<Song>>, onAdded: (String) -> Unit)

    fun playNext(songs: MutableList<Song>, onAdded: (String) -> Unit)

    fun setQueuePosition(position: Int)

    fun clearQueue()

    fun getQueue(): MutableList<Song>

    fun getQueuePosition(): Int

    fun removeFromQueue(position: Int)

    fun removeFromQueue(songs: MutableList<Song>)

    fun toggleFavorite()

    fun closeEqualizerSessions(internal: Boolean, audioSessionId: Int)

    fun openEqualizerSession(internal: Boolean, audioSessionId: Int)

    fun updateEqualizer()
}