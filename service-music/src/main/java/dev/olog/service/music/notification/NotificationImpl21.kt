package dev.olog.service.music.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Typeface
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.SpannableString
import android.text.style.StyleSpan
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import dagger.Lazy
import dev.olog.core.MediaId
import dev.olog.core.MediaIdCategory
import dev.olog.image.provider.legacy.getCachedBitmapOld
import dev.olog.service.music.R
import dev.olog.service.music.interfaces.INotification
import dev.olog.service.music.model.MusicNotificationState
import dev.olog.shared.AppConstants
import dev.olog.shared.Classes
import dev.olog.shared.extensions.asActivityPendingIntent
import dev.olog.shared.utils.assertBackgroundThread
import javax.inject.Inject

open class NotificationImpl21 @Inject constructor(
    protected val service: Service,
    private val token: MediaSessionCompat.Token,
    protected val notificationManager: Lazy<NotificationManager>

) : INotification {

    protected var builder = NotificationCompat.Builder(service, INotification.CHANNEL_ID)

    private var isCreated = false

    private fun createIfNeeded() {
        if (isCreated) {
            return
        }

        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle()
            .setMediaSession(token)
            .setShowActionsInCompactView(1, 2, 3)

        builder.setSmallIcon(R.drawable.vd_bird_not_singing)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setColor(ContextCompat.getColor(service, R.color.dark_grey))
            .setColorized(false)
            .setContentIntent(buildContentIntent())
            .setDeleteIntent(
                NotificationActions.buildMediaPendingIntent(
                    service,
                    PlaybackStateCompat.ACTION_STOP
                )
            )
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(mediaStyle)
            .addAction(NotificationActions.favorite(service, false))
            .addAction(NotificationActions.skipPrevious(service, false))
            .addAction(NotificationActions.playPause(service, false))
            .addAction(NotificationActions.skipNext(service, false))

        extendInitialization()

        isCreated = true
    }

    protected open fun extendInitialization() {}

    protected open fun startChronometer(bookmark: Long) {
    }

    protected open fun stopChronometer(bookmark: Long) {
    }

    override fun update(state: MusicNotificationState): Notification {
        assertBackgroundThread()

        createIfNeeded()

        val title = state.title
        val artist = state.artist
        val album = state.album

        val spannableTitle = SpannableString(title)
        spannableTitle.setSpan(StyleSpan(Typeface.BOLD), 0, title.length, 0)
        updateMetadataImpl(state.id, spannableTitle, artist, album, state.isPodcast)
        updateState(state.isPlaying, state.bookmark - state.duration)
        updateFavorite(state.isFavorite)

        val notification = builder.build()
        notificationManager.get().notify(INotification.NOTIFICATION_ID, notification)
        return notification
    }

    private fun updateState(isPlaying: Boolean, bookmark: Long) {
        builder.mActions[2] = NotificationActions.playPause(service, isPlaying)
        builder.setSmallIcon(if (isPlaying) R.drawable.vd_bird_singing else R.drawable.vd_bird_not_singing)
        builder.setOngoing(isPlaying)

        if (isPlaying) {
            startChronometer(bookmark)
        } else {
            stopChronometer(bookmark)
        }
    }

    private fun updateFavorite(isFavorite: Boolean) {
        builder.mActions[0] = NotificationActions.favorite(service, isFavorite)
    }

    protected open fun updateMetadataImpl(
        id: Long,
        title: SpannableString,
        artist: String,
        album: String,
        isPodcast: Boolean
    ) {
        builder.mActions[1] = NotificationActions.skipPrevious(service, isPodcast)
        builder.mActions[3] = NotificationActions.skipNext(service, isPodcast)

        val category = if (isPodcast) MediaIdCategory.PODCASTS else MediaIdCategory.SONGS
        val mediaId = MediaId.playableItem(MediaId.createCategoryValue(category, ""), id)
        val bitmap = service.getCachedBitmapOld(mediaId, INotification.IMAGE_SIZE)
        builder.setLargeIcon(bitmap)
            .setContentTitle(title)
            .setContentText(artist)
            .setSubText(album)
    }

    private fun buildContentIntent(): PendingIntent {
        val intent = Intent(service, Class.forName(Classes.ACTIVITY_MAIN))
        intent.action = AppConstants.ACTION_CONTENT_VIEW
        return intent.asActivityPendingIntent(service)
    }

    override fun cancel() {
        notificationManager.get().cancel(INotification.NOTIFICATION_ID)
    }
}