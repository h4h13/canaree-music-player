package dev.olog.presentation.edit.album

import dev.olog.core.entity.track.Album
import dev.olog.core.gateway.ImageRetrieverGateway
import dev.olog.core.gateway.track.AlbumGateway
import dev.olog.core.interactor.songlist.GetSongListByParamUseCase
import dev.olog.core.schedulers.Schedulers
import dev.olog.intents.AppConstants
import dev.olog.presentation.PresentationId
import dev.olog.presentation.toDomain
import kotlinx.coroutines.withContext
import javax.inject.Inject

class EditAlbumFragmentPresenter @Inject constructor(
    private val albumGateway: AlbumGateway,
    private val lastFmGateway: ImageRetrieverGateway,
    private val getSongListByParamUseCase: GetSongListByParamUseCase,
    private val schedulers: Schedulers

) {

    fun getAlbum(mediaId: PresentationId.Category): Album {
        val album = albumGateway.getByParam(mediaId.categoryId)!!
        return Album(
            id = album.id,
            artistId = album.artistId,
            albumArtist = album.albumArtist,
            title = album.title,
            artist = if (album.artist == AppConstants.UNKNOWN) "" else album.artist,
            hasSameNameAsFolder = album.hasSameNameAsFolder,
            songs = album.songs
        )
    }

    suspend fun getPath(mediaId: PresentationId.Category): String = withContext(schedulers.io) {
        getSongListByParamUseCase(mediaId.toDomain()).first().path
    }

}