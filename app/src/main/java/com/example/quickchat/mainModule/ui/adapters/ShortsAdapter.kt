package com.example.quickchat.mainModule.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.RecyclerView
import com.example.quickchat.databinding.ItemShortsBinding
import com.example.quickchat.mainModule.models.VideoFile

class ShortsAdapter(private val shortsList: List<VideoFile>,private val context: Context) : RecyclerView.Adapter<ShortsAdapter.ViewHolder>()  {

    private  var player: ExoPlayer= ExoPlayer.Builder(context).build()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShortsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return shortsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        Log.d("sbhjaghd",position.toString())



        val currentItem = shortsList[position]
        holder.binding.shortsView.player=player
        val mediaItem= MediaItem.fromUri(currentItem.link)
        player.setMediaItem(mediaItem)
        player.prepare()
        //player.play()

    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        player.play()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        player.stop()
    }


    class ViewHolder(val binding: ItemShortsBinding) : RecyclerView.ViewHolder(binding.root)
}
