package dev.olog.core.gateway

import dev.olog.core.entity.track.Playlist
import io.reactivex.Completable
import io.reactivex.Single

interface PodcastPlaylistGateway :
    BaseGateway<Playlist, Id>,
    ChildHasTracks<Id>,
    HasSiblings<Playlist, Id>,
    HasRelatedArtists<Id> {

    fun createPlaylist(playlistName: String): Single<Long>

    fun renamePlaylist(playlistId: Id, newTitle: String): Completable

    fun deletePlaylist(playlistId: Id): Completable

    fun clearPlaylist(playlistId: Id): Completable

    fun addSongsToPlaylist(playlistId: Id, songIds: List<Long>): Completable

    fun removeSongFromPlaylist(playlistId: Id, idInPlaylist: Long): Completable

    fun removeDuplicated(playlistId: Id): Completable


    fun insertPodcastToHistory(podcastId: Id): Completable

}