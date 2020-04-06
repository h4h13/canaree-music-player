package dev.olog.image.provider.fetcher

import android.content.Context
import android.content.SharedPreferences
import dev.olog.domain.MediaId
import dev.olog.domain.gateway.ImageRetrieverGateway

class GlideSongFetcher(
    context: Context,
    private val mediaId: MediaId.Track,
    private val imageRetrieverGateway: ImageRetrieverGateway,
    prefs: SharedPreferences
) : BaseDataFetcher(context, prefs) {

    companion object {
        private const val THRESHOLD = 600L
    }

    override suspend fun execute(): String {
        return imageRetrieverGateway.getTrack(mediaId.id.toLong())!!.image
    }

    override suspend fun mustFetch(): Boolean {
        return imageRetrieverGateway.mustFetchTrack(mediaId.id.toLong())
    }

    override val threshold: Long = THRESHOLD
}