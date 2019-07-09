package dev.olog.presentation.createplaylist

import android.util.LongSparseArray
import androidx.core.util.contains
import androidx.core.util.isEmpty
import androidx.lifecycle.*
import dev.olog.core.MediaId
import dev.olog.core.entity.PlaylistType
import dev.olog.core.gateway.podcast.PodcastGateway
import dev.olog.core.gateway.track.SongGateway
import dev.olog.core.interactor.InsertCustomTrackListRequest
import dev.olog.core.interactor.InsertCustomTrackListToPlaylist
import dev.olog.presentation.createplaylist.mapper.toDisplayableItem
import dev.olog.presentation.createplaylist.mapper.toPlaylistTrack
import dev.olog.presentation.model.DisplayableItem
import dev.olog.presentation.model.PlaylistTrack
import dev.olog.shared.extensions.*
import dev.olog.shared.extensions.filter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asObservable
import javax.inject.Inject

class CreatePlaylistFragmentViewModel @Inject constructor(
    private val playlistType: PlaylistType,
    private val getAllSongsUseCase: SongGateway,
    private val getAllPodcastsUseCase: PodcastGateway,
    private val insertCustomTrackListToPlaylist: InsertCustomTrackListToPlaylist

) : ViewModel() {

    private val data = MutableLiveData<List<DisplayableItem>>()

    private val selectedIds = LongSparseArray<Long>()
    private val selectionCountLiveData = MutableLiveData<Int>()
    private val showOnlyFiltered = ConflatedBroadcastChannel(false)

    private val filterChannel = ConflatedBroadcastChannel("")

    init {
        viewModelScope.launch {
            showOnlyFiltered.asFlow()
                .switchMap { onlyFiltered ->
                    if (onlyFiltered){
                        getPlaylistTypeTracks().map { songs -> songs.filter { selectedIds.contains(it.id) } }
                    } else {
                        getPlaylistTypeTracks().combineLatest(filterChannel.asFlow()) { tracks, filter ->
                            if (filter.isNotEmpty()) {
                                tracks.filter {
                                    it.title.contains(filter, true) ||
                                            it.artist.contains(filter, true) ||
                                            it.album.contains(filter, true)
                                }
                            } else {
                                tracks
                            }
                        }
                    }
                }.mapListItem { it.toDisplayableItem() }
                .flowOn(Dispatchers.Default)
                .collect { data.value = it }
        }
    }

    override fun onCleared() {
        viewModelScope.cancel()
    }

    fun updateFilter(filter: String) {
        filterChannel.offer(filter)
    }

    fun observeData(): LiveData<List<DisplayableItem>> = data

    private fun getPlaylistTypeTracks(): Flow<List<PlaylistTrack>> = when (playlistType) {
        PlaylistType.PODCAST -> getAllPodcastsUseCase.observeAll().mapListItem { it.toPlaylistTrack() }
        PlaylistType.TRACK -> getAllSongsUseCase.observeAll().mapListItem { it.toPlaylistTrack() }
        PlaylistType.AUTO -> throw IllegalArgumentException("type auto not valid")
    }.map { list -> list.sortedBy { it.title.toLowerCase() } }

    fun toggleItem(mediaId: MediaId) {
        val id = mediaId.resolveId
        selectedIds.toggle(id, id)
        selectionCountLiveData.postValue(selectedIds.size())
    }

    fun toggleShowOnlyFiltered() {
        val onlyFiltered = showOnlyFiltered.value
        showOnlyFiltered.offer(!onlyFiltered)

    }

    fun isChecked(mediaId: MediaId): Boolean {
        val id = mediaId.resolveId
        return selectedIds[id] != null
    }

    fun observeSelectedCount(): LiveData<Int> = selectionCountLiveData

    fun savePlaylist(playlistTitle: String): Completable {
        if (selectedIds.isEmpty()) {
            return Completable.error(IllegalStateException("empty list"))
        }
        return insertCustomTrackListToPlaylist.execute(
            InsertCustomTrackListRequest(
                playlistTitle, selectedIds.toList(), playlistType
            )
        )
    }

}