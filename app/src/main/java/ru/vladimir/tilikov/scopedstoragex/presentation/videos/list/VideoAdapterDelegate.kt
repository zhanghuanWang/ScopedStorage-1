package ru.vladimir.tilikov.scopedstoragex.presentation.videos.list

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import ru.vladimir.tilikov.scopedstoragex.R
import ru.vladimir.tilikov.scopedstoragex.data.ItemAction
import ru.vladimir.tilikov.scopedstoragex.data.Video
import ru.vladimir.tilikov.scopedstoragex.databinding.ItemVideoBinding
import ru.vladimir.tilikov.scopedstoragex.utils.haveQ
import ru.vladimir.tilikov.scopedstoragex.utils.inflate

class VideoAdapterDelegate(
    private val onClick: (id: Long, action: ItemAction) -> Unit
) : AbsListItemAdapterDelegate<Video, Video, VideoAdapterDelegate.Holder>() {

    override fun isForViewType(item: Video, items: MutableList<Video>, position: Int): Boolean {
        return true
    }

    override fun onCreateViewHolder(parent: ViewGroup): Holder {
        return Holder(parent.inflate(ItemVideoBinding::inflate), onClick)
    }

    override fun onBindViewHolder(item: Video, holder: Holder, payloads: MutableList<Any>) {
        holder.bind(item)
    }

    class Holder(
        private val binding: ItemVideoBinding,
        onClick: (id: Long, action: ItemAction) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentVideoId: Long? = null

        init {
            with(binding) {
                ivFavorite.setOnClickListener {
                    currentVideoId?.let { onClick(it, ItemAction.ADD_FAVORITE) }
                }
                ivToTrash.setOnClickListener {
                    currentVideoId?.let { onClick(it, ItemAction.TO_TRASH) }
                }
                ivDelete.setOnClickListener {
                    currentVideoId?.let { onClick(it, ItemAction.DELETE) }
                }
            }
        }

        fun bind(item: Video) {
            currentVideoId = item.id
            with(binding) {
                if (haveQ().not()) {
                    ivFavorite.visibility = View.INVISIBLE
                    ivToTrash.visibility = View.INVISIBLE
                }

                tvFileName.text = item.name
                tvSize.text = "${item.size / 1_048_576}Mb"
                when (item.isFavorite) {
                    true -> ivFavorite.setImageResource(R.drawable.ic_star)
                    false -> ivFavorite.setImageResource(R.drawable.ic_star_outline)
                }
                Glide.with(ivVideo)
                    .load(item.uri)
                    .into(ivVideo)
            }
        }
    }
}