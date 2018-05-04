package com.simplecity.amp_library.ui.detail.album

import android.content.Context
import android.view.MenuItem
import com.simplecity.amp_library.model.Album
import com.simplecity.amp_library.model.Playlist
import com.simplecity.amp_library.model.Song
import com.simplecity.amp_library.rx.UnsafeAction
import com.simplecity.amp_library.ui.presenters.Presenter
import com.simplecity.amp_library.utils.*
import io.reactivex.android.schedulers.AndroidSchedulers

class AlbumDetailPresenter @JvmOverloads constructor(private val mediaManager: MusicUtils = MusicUtils(), private val album: Album) : Presenter<AlbumDetailView>() {

    private var songs: MutableList<Song> = mutableListOf()

    private fun sortSongs(songs: MutableList<Song>) {
        @SortManager.SongSort val songSort = SortManager.getInstance().albumDetailSongsSortOrder

        val songsAscending = SortManager.getInstance().albumDetailSongsAscending

        SortManager.getInstance().sortSongs(songs, songSort)
        if (!songsAscending) {
            songs.reverse()
        }
    }

    fun loadData() {
        PermissionUtils.RequestStoragePermissions {
            addDisposable(
                album.songsSingle
                    .doOnSuccess { sortSongs(it) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { songs ->
                        this.songs = songs
                        view?.setData(songs)
                    }
            )
        }
    }

    fun closeContextualToolbar() {
        view?.closeContextualToolbar()
    }

    fun fabClicked() {
        mediaManager.shuffleAll(songs, { view?.showToast(it) })
    }

    fun playAll() {
        mediaManager.playAll(songs, 0, true) { message ->
            view?.showToast(message)
        }
    }

    fun playNext() {
        mediaManager.playNext(songs) { view?.showToast(it) }
    }

    fun addToQueue() {
        mediaManager.addToQueue(songs) { view?.showToast(it) }
    }

    fun editTags() {
        if (!ShuttleUtils.isUpgraded()) {
            view?.showUpgradeDialog()
        } else {
            view?.showTaggerDialog()
        }
    }

    fun editArtwork() {
        view?.showArtworkDialog()
    }

    fun showBio() {
        view?.showBioDialog()
    }

    fun newPlaylist() {
        view?.showCreatePlaylistDialog(songs.toList())
    }

    fun playlistSelected(context: Context, item: MenuItem, insertCallback: UnsafeAction) {
        val playlist = item.intent.getSerializableExtra(PlaylistUtils.ARG_PLAYLIST) as Playlist
        PlaylistUtils.addToPlaylist(context, playlist, songs, insertCallback)
    }

    fun songClicked(song: Song) {
        mediaManager.playAll(songs, songs.indexOf(song), true) { view?.showToast(it) }
    }
}
