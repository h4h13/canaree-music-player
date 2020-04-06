package dev.olog.presentation.playlist.chooser

import androidx.fragment.app.FragmentActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.olog.appshortcuts.AppShortcuts
import dev.olog.domain.schedulers.Schedulers
import dev.olog.presentation.R
import dev.olog.presentation.base.adapter.DataBoundViewHolder
import dev.olog.presentation.base.adapter.DiffCallbackDisplayableItem
import dev.olog.presentation.base.adapter.ObservableAdapter
import dev.olog.presentation.base.adapter.setOnClickListener
import dev.olog.presentation.loadAlbumImage
import dev.olog.presentation.model.DisplayableAlbum
import dev.olog.presentation.model.DisplayableItem
import dev.olog.presentation.toDomain
import kotlinx.android.synthetic.main.item_tab_album.view.*

class PlaylistChooserActivityAdapter(
    private val activity: FragmentActivity,
    private val schedulers: Schedulers
) : ObservableAdapter<DisplayableItem>(DiffCallbackDisplayableItem) {

    override fun initViewHolderListeners(viewHolder: DataBoundViewHolder, viewType: Int) {
        viewHolder.setOnClickListener(this) { item, _, _ ->
            askConfirmation(item)
        }
    }

    private fun askConfirmation(item: DisplayableItem) {
        require(item is DisplayableAlbum)

        MaterialAlertDialogBuilder(activity)
            .setTitle(R.string.playlist_chooser_dialog_title)
            .setMessage(activity.getString(R.string.playlist_chooser_dialog_message, item.title))
            .setPositiveButton(R.string.popup_positive_ok) { _, _ ->
                AppShortcuts.instance(activity, schedulers)
                    .addDetailShortcut(item.mediaId.toDomain(), item.title)
                activity.finish()
            }
            .setNegativeButton(R.string.popup_negative_no, null)
            .show()
    }

    override fun bind(holder: DataBoundViewHolder, item: DisplayableItem, position: Int) {
        require(item is DisplayableAlbum)

        holder.itemView.apply {
            holder.imageView!!.loadAlbumImage(item.mediaId.toDomain())
            firstText.text = item.title
            secondText.text = item.subtitle
        }
    }
}