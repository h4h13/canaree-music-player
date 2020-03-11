package dev.olog.presentation.relatedartists

import android.content.res.Resources
import androidx.lifecycle.ViewModel
import dev.olog.core.entity.track.Artist
import dev.olog.core.interactor.GetItemTitleUseCase
import dev.olog.core.interactor.ObserveRelatedArtistsUseCase
import dev.olog.core.schedulers.Schedulers
import dev.olog.presentation.PresentationId
import dev.olog.presentation.R
import dev.olog.presentation.model.DisplayableAlbum
import dev.olog.presentation.presentationId
import dev.olog.presentation.toDomain
import dev.olog.shared.mapListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RelatedArtistFragmentViewModel @Inject constructor(
    resources: Resources,
    mediaId: PresentationId.Category,
    useCase: ObserveRelatedArtistsUseCase,
    getItemTitleUseCase: GetItemTitleUseCase,
    schedulers: Schedulers

) : ViewModel() {

    val itemOrdinal = mediaId.category.ordinal // TODO try to remove ordinal

    val data: Flow<List<DisplayableAlbum>> = useCase(mediaId.toDomain())
        .mapListItem { it.toRelatedArtist(resources) }
        .flowOn(schedulers.io)

    val title: Flow<String> = getItemTitleUseCase(mediaId.toDomain())
        .flowOn(schedulers.io)
        .map { it ?: "" }


    private fun Artist.toRelatedArtist(resources: Resources): DisplayableAlbum {
        val songs =
            resources.getQuantityString(R.plurals.common_plurals_song, this.songs, this.songs)

        return DisplayableAlbum(
            type = R.layout.item_related_artist,
            mediaId = presentationId,
            title = this.name,
            subtitle = songs
        )
    }

}