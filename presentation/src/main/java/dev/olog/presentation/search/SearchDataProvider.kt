package dev.olog.presentation.search

import android.content.Context
import dev.olog.core.gateway.RecentSearchesGateway
import dev.olog.core.gateway.podcast.PodcastAuthorGateway
import dev.olog.core.gateway.podcast.PodcastGateway
import dev.olog.core.gateway.podcast.PodcastPlaylistGateway
import dev.olog.core.gateway.track.*
import dev.olog.presentation.PresentationId
import dev.olog.presentation.PresentationId.Companion.headerId
import dev.olog.presentation.R
import dev.olog.presentation.model.DisplayableAlbum
import dev.olog.presentation.model.DisplayableHeader
import dev.olog.presentation.model.DisplayableItem
import dev.olog.shared.ApplicationContext
import dev.olog.shared.android.extensions.assertBackground
import dev.olog.shared.mapListItem
import dev.olog.shared.startWithIfNotEmpty
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class SearchDataProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val searchHeaders: SearchFragmentHeaders,
    private val folderGateway: FolderGateway,
    private val playlistGateway2: PlaylistGateway,
    private val songGateway: SongGateway,
    private val albumGateway: AlbumGateway,
    private val artistGateway: ArtistGateway,
    private val genreGateway: GenreGateway,
    // podcasts
    private val podcastPlaylistGateway: PodcastPlaylistGateway,
    private val podcastGateway: PodcastGateway,
    private val podcastAuthorGateway: PodcastAuthorGateway,
    // recent
    private val recentSearchesGateway: RecentSearchesGateway

) {

    private val queryChannel = ConflatedBroadcastChannel("")

    fun updateQuery(query: String) {
        queryChannel.offer(query)
    }

    fun observe(): Flow<List<DisplayableItem>> {
        return queryChannel.asFlow().flatMapLatest { query ->
            if (query.isBlank()) {
                getRecents()
            } else {
                getFiltered(query)
            }
        }.assertBackground()
    }

    fun observeArtists(): Flow<List<DisplayableAlbum>> {
        return queryChannel.asFlow()
            .flatMapLatest { getArtists(it) }
            .assertBackground()
    }

    fun observeAlbums(): Flow<List<DisplayableAlbum>> {
        return queryChannel.asFlow()
            .flatMapLatest { getAlbums(it) }
            .assertBackground()
    }

    fun observeGenres(): Flow<List<DisplayableAlbum>> {
        return queryChannel.asFlow()
            .flatMapLatest { getGenres(it) }
            .assertBackground()
    }

    fun observePlaylists(): Flow<List<DisplayableAlbum>> {
        return queryChannel.asFlow()
            .flatMapLatest { getPlaylists(it) }
            .assertBackground()
    }

    fun observeFolders(): Flow<List<DisplayableAlbum>> {
        return queryChannel.asFlow()
            .flatMapLatest { getFolders(it) }
            .assertBackground()
    }

    private fun getRecents(): Flow<List<DisplayableItem>> {
        return recentSearchesGateway.getAll()
            .mapListItem { it.toSearchDisplayableItem(context) }
            .map { it.toMutableList() }
            .map {
                if (it.isNotEmpty()) {
                    it.add(
                        DisplayableHeader(
                            type = R.layout.item_search_clear_recent,
                            mediaId = headerId("clear recent"),
                            title = ""
                        )
                    )
                    it.addAll(0, searchHeaders.recents)
                }
                it
            }
    }

    private fun getFiltered(query: String): Flow<List<DisplayableItem>> {
        return combine(
                getArtists(query).map { if (it.isNotEmpty()) searchHeaders.artistsHeaders(it.size) else it },
                getAlbums(query).map { if (it.isNotEmpty()) searchHeaders.albumsHeaders(it.size) else it },
                getPlaylists(query).map { if (it.isNotEmpty()) searchHeaders.playlistsHeaders(it.size) else it },
                getGenres(query).map { if (it.isNotEmpty()) searchHeaders.genreHeaders(it.size) else it },
                getFolders(query).map { if (it.isNotEmpty()) searchHeaders.foldersHeaders(it.size) else it },
                getSongs(query)
            ) { list -> list.toList().flatten() }
    }

    private fun getSongs(query: String): Flow<List<DisplayableItem>> {
        return songGateway.observeAll().map { list ->
            list.asSequence()
                .filter {
                    it.title.contains(query, true) ||
                            it.artist.contains(query, true) ||
                            it.album.contains(query, true)
                }.map { it.toSearchDisplayableItem() }
                .toList()
        }.combine(
            podcastGateway.observeAll().map { list ->
                list.asSequence()
                    .filter {
                        it.title.contains(query, true) ||
                                it.artist.contains(query, true) ||
                                it.album.contains(query, true)
                    }.map { it.toSearchDisplayableItem() }
                    .toList()
            }
        ) { track, podcast ->
            val result = (track + podcast).sortedBy { it.title }
            result.startWithIfNotEmpty(searchHeaders.songsHeaders(result.size))
        }
    }

    private fun getAlbums(query: String): Flow<List<DisplayableAlbum>> {
        return albumGateway.observeAll().map { list ->
            if (query.isBlank()) {
                return@map listOf<DisplayableAlbum>()
            }
            list.asSequence()
                .filter {
                    it.title.contains(query, true) || it.artist.contains(query, true)
                }.map { it.toSearchDisplayableItem() }
                .toList()
                .sortedBy { it.title }
        }
    }

    private fun getArtists(query: String): Flow<List<DisplayableAlbum>> {
        return artistGateway.observeAll().map { list ->
            if (query.isBlank()) {
                return@map listOf<DisplayableAlbum>()
            }
            list.asSequence()
                .filter { it.name.contains(query, true) }
                .map { it.toSearchDisplayableItem() }
                .toList()
        }.combine(
            podcastAuthorGateway.observeAll().map { list ->
                if (query.isBlank()) {
                    return@map listOf<DisplayableAlbum>()
                }
                list.asSequence()
                    .filter {
                        it.name.contains(query, true)
                    }.map { it.toSearchDisplayableItem() }
                    .toList()
            }
        ) { track, podcast ->
            (track + podcast).sortedBy { it.title }
        }
    }

    private fun getPlaylists(query: String): Flow<List<DisplayableAlbum>> {
        return playlistGateway2.observeAll().map { list ->
            if (query.isBlank()) {
                return@map listOf<DisplayableAlbum>()
            }
            list.asSequence()
                .filter {
                    it.title.contains(query, true)
                }.map { it.toSearchDisplayableItem() }
                .toList()
        }.combine(
            podcastPlaylistGateway.observeAll().map { list ->
                if (query.isBlank()) {
                    return@map listOf<DisplayableAlbum>()
                }

                list.asSequence()
                    .filter {
                        it.title.contains(query, true)
                    }.map { it.toSearchDisplayableItem() }
                    .toList()
            }
        ) { track, podcast ->
            (track + podcast).sortedBy { it.title }
        }
    }

    private fun getGenres(query: String): Flow<List<DisplayableAlbum>> {
        return genreGateway.observeAll().map { list ->
            if (query.isBlank()) {
                return@map listOf<DisplayableAlbum>()
            }
            list.asSequence()
                .filter {
                    it.name.contains(query, true)
                }.map { it.toSearchDisplayableItem() }
                .toList()
        }
    }

    private fun getFolders(query: String): Flow<List<DisplayableAlbum>> {
        return folderGateway.observeAll().map { list ->
            if (query.isBlank()) {
                return@map listOf<DisplayableAlbum>()
            }
            list.asSequence()
                .filter {
                    it.title.contains(query, true)
                }.map { it.toSearchDisplayableItem() }
                .toList()
        }
    }

}