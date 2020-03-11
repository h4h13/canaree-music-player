package dev.olog.presentation.createplaylist.mapper

import dev.olog.core.entity.track.Song
import dev.olog.presentation.R
import dev.olog.presentation.model.DisplayableTrack
import dev.olog.presentation.presentationId

internal fun Song.toDisplayableItem(): DisplayableTrack {
    return DisplayableTrack(
        type = R.layout.item_create_playlist,
        mediaId = presentationId,
        title = this.title,
        artist = this.artist,
        album = this.album,
        idInPlaylist = this.idInPlaylist,
        dataModified = this.dateModified,
        duration = this.duration
    )
}