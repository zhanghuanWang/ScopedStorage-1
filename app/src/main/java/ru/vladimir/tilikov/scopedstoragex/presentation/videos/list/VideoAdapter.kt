package ru.vladimir.tilikov.scopedstoragex.presentation.videos.list

import androidx.recyclerview.widget.DiffUtil
import com.hannesdorfmann.adapterdelegates4.AsyncListDifferDelegationAdapter
import ru.vladimir.tilikov.scopedstoragex.data.ItemAction
import ru.vladimir.tilikov.scopedstoragex.data.Video

class VideoAdapter(
    onClick: (id: Long, action: ItemAction) -> Unit
): AsyncListDifferDelegationAdapter<Video>(VideoDiffUtilCallback()) {

    init {
        delegatesManager.addDelegate(VideoAdapterDelegate(onClick))
    }

    class VideoDiffUtilCallback: DiffUtil.ItemCallback<Video>() {
        override fun areItemsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Video, newItem: Video): Boolean {
            return oldItem == newItem
        }
    }
}